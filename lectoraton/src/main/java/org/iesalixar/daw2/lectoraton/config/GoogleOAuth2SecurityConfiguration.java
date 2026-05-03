package org.iesalixar.daw2.lectoraton.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Login OAuth2 con Google ({@code /oauth2/authorization/google}).
 * Solo se registra cuando existen cliente y secreto válidos ({@link GoogleOAuth2EnabledCondition}).
 * Tras éxito, {@link OAuth2LoginSuccessHandler} invalida sesión servidor y envía JWT al frontal en fragmento URL.
 */
@Configuration
@Conditional(GoogleOAuth2EnabledCondition.class)
public class GoogleOAuth2SecurityConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuth2SecurityConfiguration.class);

    @Bean
    @Order(0)
    public SecurityFilterChain googleOAuthSecurityFilterChain(
            HttpSecurity http,
            OAuth2LoginSuccessHandler oauth2LoginSuccessHandler,
            OAuth2LoginFailureHandler oauth2LoginFailureHandler) throws Exception {

        logger.info("OAuth2 cliente Google registrado — rutas /oauth2/** y /login/oauth2/** activas.");

        http.securityMatcher("/oauth2/**", "/login/oauth2/**")
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth -> oauth
                        .successHandler(oauth2LoginSuccessHandler)
                        .failureHandler(oauth2LoginFailureHandler));

        return http.build();
    }
}
