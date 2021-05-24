package org.apereo.cas.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link ConditionalOnWebAuthnEnabled}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@ConditionalOnProperty(prefix = "cas.authn.mfa.web-authn.core", name = "enabled", havingValue = "true", matchIfMissing = true)
public @interface ConditionalOnWebAuthnEnabled {
}
