package org.iesalixar.daw2.lectoraton.services;

import jakarta.mail.internet.MimeMessage;
import org.iesalixar.daw2.lectoraton.entities.Usuario;
import org.iesalixar.daw2.lectoraton.utils.EditorPromotionTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Correos del flujo de rol Editor: solicitud al administrador y aviso al usuario al aprobar.
 */
@Service
public class EditorPromotionMailService {

    private static final Logger logger = LoggerFactory.getLogger(EditorPromotionMailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final EditorPromotionTokenUtil tokenUtil;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${lectoraton.mail.from:}")
    private String mailFromOverride;

    @Value("${lectoraton.editor-request.to:programagons@gmail.com}")
    private String mailTo;

    @Value("${lectoraton.app-public-url:http://localhost:8080}")
    private String appPublicUrl;

    public EditorPromotionMailService(ObjectProvider<JavaMailSender> mailSenderProvider, EditorPromotionTokenUtil tokenUtil) {
        this.mailSenderProvider = mailSenderProvider;
        this.tokenUtil = tokenUtil;
    }

    /**
     * Envia un correo de solicitud de rol Editor al administrador.
     *
     * @param solicitante Usuario que solicita el rol Editor.
     * @throws IllegalStateException si el correo no está configurado.
     */
    public void enviarSolicitud(Usuario solicitante) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || mailUsername == null || mailUsername.isBlank()) {
            throw new IllegalStateException(
                    "Correo no configurado: define MAIL_USER, MAIL_PASS y MAIL_HOST (o spring.mail.*) en el servidor.");
        }
        String from = mailFromOverride != null && !mailFromOverride.isBlank() ? mailFromOverride : mailUsername;

        String token = tokenUtil.crearToken(solicitante.getId());
        String approveUrl = appPublicUrl.replaceAll("/$", "") + "/api/v1/editor-requests/approve?token="
                + java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8);

        String nombreCompleto = (solicitante.getNombre() + " " + solicitante.getApellidos()).trim();

        /**
         * Genera el HTML del correo de solicitud de rol Editor.
         *
         * @param solicitante Usuario que solicita el rol Editor.
         * @param nombreCompleto Nombre completo del usuario.
         * @param email Email del usuario.
         * @param id Id del usuario.
         * @param approveUrl URL de aprobación del rol Editor.
         * @return HTML del correo de solicitud de rol Editor.
         */
        String html = """
                <!DOCTYPE html>
                <html><head><meta charset="UTF-8"/></head>
                <body style="font-family:Segoe UI,Roboto,sans-serif;background:#f5eedd;padding:24px;color:#3d2b20;">
                  <table width="100%%" style="max-width:560px;margin:0 auto;background:#fff;border-radius:12px;
                         box-shadow:0 6px 24px rgba(0,0,0,.08);overflow:hidden;">
                    <tr><td style="background:#ebd8ae;padding:18px 22px;">
                      <h1 style="margin:0;font-size:20px;color:#5c4033;">Lectoratón — Solicitud de rol Editor</h1>
                    </td></tr>
                    <tr><td style="padding:22px;">
                      <p style="margin:0 0 14px;">Un usuario ha solicitado poder añadir libros en la plataforma (como editor).</p>
                      <table style="width:100%%;border-collapse:collapse;font-size:14px;">
                        <tr><td style="padding:8px 0;border-bottom:1px solid #eee;"><strong>Usuario</strong></td>
                            <td style="padding:8px 0;border-bottom:1px solid #eee;">%s</td></tr>
                        <tr><td style="padding:8px 0;border-bottom:1px solid #eee;"><strong>Nombre</strong></td>
                            <td style="padding:8px 0;border-bottom:1px solid #eee;">%s</td></tr>
                        <tr><td style="padding:8px 0;border-bottom:1px solid #eee;"><strong>Email</strong></td>
                            <td style="padding:8px 0;border-bottom:1px solid #eee;">%s</td></tr>
                        <tr><td style="padding:8px 0;border-bottom:1px solid #eee;"><strong>Id interno</strong></td>
                            <td style="padding:8px 0;border-bottom:1px solid #eee;">%d</td></tr>
                      </table>
                      <p style="margin:20px 0 12px;font-size:13px;color:#666;">Si reconoces la solicitud, pulsa el botón.
                         El enlace caduca en 14 días.</p>
                      <a href="%s" style="display:inline-block;background:#ad7b59;color:#fff;text-decoration:none;
                         padding:12px 22px;border-radius:8px;font-weight:600;">Conceder rol editor</a>
                      <p style="margin-top:18px;font-size:12px;color:#888;">Tras aprobar, el usuario debe <strong>cerrar sesión e iniciar sesión de nuevo</strong> para que su token incluya el nuevo rol.</p>
                    </td></tr>
                  </table>
                </body></html>
                """
                .formatted(
                        escapeHtml(solicitante.getUsername()),
                        escapeHtml(nombreCompleto),
                        escapeHtml(solicitante.getEmail()),
                        solicitante.getId(),
                        approveUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(mailTo);
            helper.setSubject("[Lectoratón] Solicitud de rol Editor — @" + solicitante.getUsername());
            helper.setText(html, true);
            mailSender.send(message);
            logger.info("Correo de solicitud editor enviado para usuario id {}", solicitante.getId());
        } catch (Exception e) {
            logger.error("No se pudo enviar el correo de solicitud editor: {}", e.getMessage());
            throw new IllegalStateException("No se pudo enviar el correo. Revisa la configuración SMTP.", e);
        }
    }

    /**
     * Avisa al usuario cuando su rol Editor ha sido aprobado.
     * Si SMTP no está configurado o el envío falla, solo se registra en log (la aprobación ya está guardada).
     */
    public void enviarAprobacionEditor(Usuario usuario) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || mailUsername == null || mailUsername.isBlank()) {
            logger.warn("No se envía correo de aprobación editor: SMTP no configurado.");
            return;
        }
        String destino = usuario.getEmail();
        if (destino == null || destino.isBlank()) {
            logger.warn("Usuario id {} sin email; no se envía aviso de aprobación editor.", usuario.getId());
            return;
        }
        String from = mailFromOverride != null && !mailFromOverride.isBlank() ? mailFromOverride : mailUsername;

        String saludo;
        if (usuario.getNombre() != null && !usuario.getNombre().isBlank()) {
            saludo = escapeHtml(usuario.getNombre().trim());
        } else {
            saludo = escapeHtml(usuario.getUsername());
        }

        String html = """
                <!DOCTYPE html>
                <html><head><meta charset="UTF-8"/></head>
                <body style="font-family:Segoe UI,Roboto,sans-serif;background:#f5eedd;padding:24px;color:#3d2b20;">
                  <div style="max-width:520px;margin:0 auto;background:#fff;padding:22px;border-radius:12px;
                              box-shadow:0 4px 18px rgba(0,0,0,.06);">
                    <p style="margin:0 0 12px;">Hola %s,</p>
                    <p style="margin:0 0 12px;line-height:1.5;">Tu solicitud para tener rol <strong>editor</strong> en Lectoratón ha sido <strong>aceptada</strong>.</p>
                    <p style="margin:0 0 12px;line-height:1.5;">Ya puedes añadir libros al catálogo.</p>
                    <p style="margin:0;line-height:1.5;font-size:14px;color:#555;"><strong>Importante:</strong> cierra sesión e inicia sesión de nuevo para que la aplicación aplique tu nuevo permiso.</p>
                    <p style="margin:18px 0 0;color:#888;font-size:13px;">— Lectoratón</p>
                  </div>
                </body></html>
                """.formatted(saludo);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(destino);
            helper.setSubject("[Lectoratón] Tu solicitud de editor ha sido aceptada");
            helper.setText(html, true);
            mailSender.send(message);
            logger.info("Correo de aprobación editor enviado a usuario id {}", usuario.getId());
        } catch (Exception e) {
            logger.warn("No se pudo enviar el correo de aprobación editor a {}: {}", destino, e.getMessage());
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
