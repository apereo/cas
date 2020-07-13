package org.apereo.cas.adaptors.duo.config.cond;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link ConditionalOnDuoSecurityConfigured}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ConditionalOnProperty({
    "cas.authn.mfa.duo[0].duo-api-host",
    "cas.authn.mfa.duo[0].duo-integration-key",
    "cas.authn.mfa.duo[0].duo-application-key",
    "cas.authn.mfa.duo[0].duo-secret-key"
})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface ConditionalOnDuoSecurityConfigured {
}
