package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.vc.issuer.OidcCredentialIssuerMetadataController;
import org.apereo.cas.oidc.vc.issuer.OidcCredentialIssuerMetadataService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link OidcVerifiableCredentialsIssuerConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "OidcVerifiableCredentialsIssuerConfiguration", proxyBeanMethods = false)
class OidcVerifiableCredentialsIssuerConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcCredentialIssuerMetadataService")
    @Bean
    public OidcCredentialIssuerMetadataService oidcCredentialIssuerMetadataService(
        final CasConfigurationProperties casProperties) {
        return new OidcCredentialIssuerMetadataService(casProperties);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcCredentialIssuerMetadataController")
    @Bean
    public OidcCredentialIssuerMetadataController oidcCredentialIssuerMetadataController(
        final CasConfigurationProperties casProperties,
        final OidcCredentialIssuerMetadataService metadataService) {
        return new OidcCredentialIssuerMetadataController(metadataService);
    }
}
