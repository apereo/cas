package org.apereo.cas.web.flow.config;

import org.apereo.cas.util.spring.boot.ConditionalOnMultiValuedProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link ConditionalOnSaml2ClientConfigured}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@ConditionalOnMultiValuedProperty(name = "cas.authn.pac4j.saml[0]",
    value = {"service-provider-metadata-path", "identity-provider-metadata-path"})
public @interface ConditionalOnSaml2ClientConfigured {
}
