package org.iesalixar.daw2.lectoraton.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.iesalixar.daw2.lectoraton.entities.Usuario;
import org.iesalixar.daw2.lectoraton.services.UsuarioService;
import org.iesalixar.daw2.lectoraton.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Después del login OAuth2 con Google genera el mismo JWT que {@code /api/v1/authenticate} y redirecciona
 * al frontal Angular con {@code #token=} en el fragmento (no suele aparecer en cabeceras Referer como query).
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final JwtUtil jwtUtil;
    private final UsuarioService usuarioService;

    @Value("${lectoraton.oauth2.frontend-url:http://localhost:4200}")
    private String frontendBaseUrl;

    public OAuth2LoginSuccessHandler(JwtUtil jwtUtil, UsuarioService usuarioService) {
        this.jwtUtil = jwtUtil;
        this.usuarioService = usuarioService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException {
        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            response.sendRedirect(sanitizeFrontend() + "/?oauthError=no_oauth_principal");
            return;
        }

        OAuth2User principal = token.getPrincipal();
        String email = principal.getAttribute("email");
        if (email == null || email.isBlank()) {
            response.sendRedirect(sanitizeFrontend() + "/?oauthError=missing_email");
            return;
        }

        Boolean verified = principal.getAttribute("email_verified");
        if (Boolean.FALSE.equals(verified)) {
            response.sendRedirect(sanitizeFrontend() + "/?oauthError=email_unverified");
            return;
        }

        String givenName = principal.getAttribute("given_name");
        String familyName = principal.getAttribute("family_name");
        String fullName = principal.getAttribute("name");
        if ((givenName == null || givenName.isBlank()) && fullName != null && !fullName.isBlank()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            givenName = parts[0];
            familyName = parts.length > 1 ? parts[1] : "-";
        }
        if (givenName == null || givenName.isBlank()) {
            givenName = email.contains("@") ? email.substring(0, email.indexOf('@')) : "Usuario";
        }
        if (familyName == null || familyName.isBlank()) {
            familyName = "-";
        }

        String picture = principal.getAttribute("picture");
        try {
            Usuario usuario = usuarioService.findOrProvisionFromGoogleOAuth(
                    email, givenName, familyName, picture);
            List<String> roles = usuario.getRoles().stream().map(r -> r.getNombre()).sorted().toList();
            String jwt = jwtUtil.generateToken(usuario.getUsername(), roles, usuario.getId());

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            String encoded = URLEncoder.encode(jwt, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            response.sendRedirect(sanitizeFrontend() + "/#token=" + encoded);

        } catch (Exception e) {
            logger.warn("Fallo tras login Google para {}: {}", email, e.getMessage());
            response.sendRedirect(sanitizeFrontend() + "/?oauthError=provision_failed");
        }
    }

    private String sanitizeFrontend() {
        return frontendBaseUrl == null ? "http://localhost:4200" : frontendBaseUrl.replaceAll("/$", "");
    }
}
