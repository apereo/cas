package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationResponseEndpointController;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link OidcVerifiableCredentialsPresentationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "OidcVerifiableCredentialsPresentationConfiguration", proxyBeanMethods = false)
class OidcVerifiableCredentialsPresentationConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcVerifiableCredentialPresentationRequestEndpointController")
    public OidcVerifiableCredentialPresentationRequestEndpointController oidcVerifiableCredentialPresentationRequestEndpointController(
        @Qualifier(OidcConfigurationContext.BEAN_NAME)
        final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcVerifiableCredentialPresentationRequestEndpointController(oidcConfigurationContext);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcVerifiableCredentialPresentationResponseEndpointController")
    public OidcVerifiableCredentialPresentationResponseEndpointController oidcVerifiableCredentialPresentationResponseEndpointController(
        @Qualifier(OidcConfigurationContext.BEAN_NAME)
        final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcVerifiableCredentialPresentationResponseEndpointController(oidcConfigurationContext);
    }

}
