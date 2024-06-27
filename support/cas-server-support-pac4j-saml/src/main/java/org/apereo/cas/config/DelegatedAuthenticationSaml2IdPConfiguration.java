package org.apereo.cas.config;

import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlIdPResponseCustomizer;
import org.apereo.cas.web.saml2.DelegatedAuthenticationSamlIdPResponseCustomizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link DelegatedAuthenticationSaml2IdPConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ConditionalOnClass(SamlIdPResponseCustomizer.class)
@Configuration(value = "DelegatedAuthenticationSaml2IdPConfiguration", proxyBeanMethods = false)
class DelegatedAuthenticationSaml2IdPConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "delegatedSaml2IdPResponseCustomizer")
    public SamlIdPResponseCustomizer delegatedSaml2IdPResponseCustomizer(
        @Qualifier(DelegatedIdentityProviders.BEAN_NAME) final DelegatedIdentityProviders identityProviders) {
        return new DelegatedAuthenticationSamlIdPResponseCustomizer(identityProviders);
    }
}

