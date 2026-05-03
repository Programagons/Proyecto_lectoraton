package org.iesalixar.daw2.lectoraton.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    @Value("${lectoraton.oauth2.frontend-url:http://localhost:4200}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String base = frontendBaseUrl == null ? "http://localhost:4200" : frontendBaseUrl.replaceAll("/$", "");
        response.sendRedirect(base + "/?oauthError=oauth_failed");
    }
}
