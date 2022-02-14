package org.apereo.cas.config;

import org.apereo.cas.util.spring.boot.ConditionalOnMultiValuedProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link ConditionalOnLdapAuthenticationEnabled}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@ConditionalOnMultiValuedProperty(name = "cas.authn.ldap[0]", value = {"ldap-url", "type"})
public @interface ConditionalOnLdapAuthenticationEnabled {
}
