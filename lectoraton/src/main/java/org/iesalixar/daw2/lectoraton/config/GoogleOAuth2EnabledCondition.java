package org.iesalixar.daw2.lectoraton.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * Activa OAuth2 con Google sólo cuando hay cliente y secreto configurados (variables de entorno o properties).
 */
public class GoogleOAuth2EnabledCondition implements Condition {

    private static final String PREFIX = "spring.security.oauth2.client.registration.google.";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var env = context.getEnvironment();
        return StringUtils.hasText(env.getProperty(PREFIX + "client-id"))
                && StringUtils.hasText(env.getProperty(PREFIX + "client-secret"));
    }
}
