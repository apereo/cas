package org.apereo.cas.adaptors.duo.config.cond;

import org.apereo.cas.util.spring.boot.ConditionalOnMultiValuedProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link ConditionalOnDuoSecurityAdminApiConfigured}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@ConditionalOnMultiValuedProperty(name = "cas.authn.mfa.duo[0]",
    value = {"duo-api-host", "duo-admin-integration-key", "duo-admin-secret-key"})
public @interface ConditionalOnDuoSecurityAdminApiConfigured {
}
