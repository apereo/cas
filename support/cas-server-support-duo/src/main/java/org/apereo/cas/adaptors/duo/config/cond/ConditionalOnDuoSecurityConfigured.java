package org.apereo.cas.adaptors.duo.config.cond;

import org.apereo.cas.util.spring.boot.ConditionalOnMultiValuedProperty;

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
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@ConditionalOnMultiValuedProperty(name = "cas.authn.mfa.duo[0]",
    value = {"duo-api-host", "duo-integration-key", "duo-secret-key"})
public @interface ConditionalOnDuoSecurityConfigured {
}
