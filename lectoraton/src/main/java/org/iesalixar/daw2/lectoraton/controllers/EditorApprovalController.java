package org.iesalixar.daw2.lectoraton.controllers;

import org.iesalixar.daw2.lectoraton.services.UsuarioService;
import org.iesalixar.daw2.lectoraton.utils.EditorPromotionTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Enlace del correo para conceder el rol Editor (token firmado, sin JWT).
 */
@RestController
@RequestMapping("/api/v1/editor-requests")
public class EditorApprovalController {

    private static final Logger logger = LoggerFactory.getLogger(EditorApprovalController.class);

    private final EditorPromotionTokenUtil tokenUtil;
    private final UsuarioService usuarioService;

    public EditorApprovalController(EditorPromotionTokenUtil tokenUtil, UsuarioService usuarioService) {
        this.tokenUtil = tokenUtil;
        this.usuarioService = usuarioService;
    }

    /**
     * Endpoint para aprobar la solicitud de editor.
     *
     * @param token Token de la solicitud de editor.
     * @return ResponseEntity con el HTML de la página de aprobación o error en caso de fallo.
     */
    @GetMapping(value = "/approve", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> approve(@RequestParam("token") String token) {
        Long usuarioId = tokenUtil.validarYUsuarioId(token);
        if (usuarioId == null) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_HTML).body(htmlPagina(
                    "Enlace no válido o caducado",
                    "Genera una nueva solicitud desde el perfil o revisa el enlace del correo."));
        }
        try {
            usuarioService.otorgarRolEditor(usuarioId);
            logger.info("Rol Editor otorgado vía correo a usuario id {}", usuarioId);
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(htmlPagina(
                    "Editor activado",
                    "El usuario ya puede añadir libros tras iniciar sesión de nuevo en la aplicación."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_HTML).body(htmlPagina(
                    "No se pudo completar", escapeHtml(e.getMessage())));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).contentType(MediaType.TEXT_HTML).body(htmlPagina(
                    "Error de configuración", escapeHtml(e.getMessage())));
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * Genera el HTML de la página de aprobación.
     *
     * @param titulo Título de la página.
     * @param mensaje Mensaje de la página.
     * @return HTML de la página de aprobación.
     */
    private static String htmlPagina(String titulo, String mensaje) {
        return """
                <!DOCTYPE html><html lang="es"><head><meta charset="UTF-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1"/>
                <title>%s</title></head>
                <body style="font-family:system-ui,sans-serif;background:#f5eedd;color:#3d2b20;padding:32px;">
                  <div style="max-width:480px;margin:0 auto;background:#fff;padding:28px;border-radius:12px;
                              box-shadow:0 8px 28px rgba(0,0,0,.1);">
                    <h1 style="margin:0 0 12px;font-size:1.35rem;color:#5c4033;">%s</h1>
                    <p style="margin:0;line-height:1.5;">%s</p>
                  </div>
                </body></html>
                """.formatted(escapeHtml(titulo), escapeHtml(titulo), mensaje);
    }
}
