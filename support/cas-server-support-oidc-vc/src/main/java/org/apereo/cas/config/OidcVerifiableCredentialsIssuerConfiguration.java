package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.vc.issuer.OidcDefaultVerifiableCredentialIssuerService;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialIssuerService;
import org.apereo.cas.oidc.vc.issuer.enc.OidcVerifiableCredentialDcSdJwtEncoder;
import org.apereo.cas.oidc.vc.issuer.enc.OidcVerifiableCredentialEncoder;
import org.apereo.cas.oidc.vc.issuer.enc.OidcVerifiableCredentialEncoderFactory;
import org.apereo.cas.oidc.vc.issuer.enc.OidcVerifiableCredentialJwtVcJsonEncoder;
import org.apereo.cas.oidc.vc.issuer.enc.OidcVerifiableCredentialJwtVcJsonLdEncoder;
import org.apereo.cas.oidc.vc.issuer.metadata.OidcCredentialIssuerMetadataService;
import org.apereo.cas.oidc.vc.issuer.nonce.OidcVerifiableCredentialDefaultNonceService;
import org.apereo.cas.oidc.vc.issuer.nonce.OidcVerifiableCredentialNonceService;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialJwtProofValidator;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator;
import org.apereo.cas.oidc.vc.issuer.web.OidcVerifiableCredentialEndpointController;
import org.apereo.cas.oidc.vc.issuer.web.OidcVerifiableCredentialIssuerMetadataController;
import org.apereo.cas.oidc.vc.issuer.web.OidcVerifiableCredentialNonceEndpointController;
import org.apereo.cas.oidc.vc.issuer.web.OidcVerifiableCredentialTypeMetadataController;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcVerifiableCredentialDcSdJwtEncoder")
    public OidcVerifiableCredentialEncoder oidcVerifiableCredentialDcSdJwtEncoder(
        @Qualifier(OidcConfigurationContext.BEAN_NAME) final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcVerifiableCredentialDcSdJwtEncoder(oidcConfigurationContext);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcVerifiableCredentialJwtVcJsonEncoder")
    public OidcVerifiableCredentialEncoder oidcVerifiableCredentialJwtVcJsonEncoder(
        @Qualifier(OidcConfigurationContext.BEAN_NAME) final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcVerifiableCredentialJwtVcJsonEncoder(oidcConfigurationContext);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcVerifiableCredentialJwtVcJsonLdEncoder")
    public OidcVerifiableCredentialEncoder oidcVerifiableCredentialJwtVcJsonLdEncoder(
        @Qualifier(OidcConfigurationContext.BEAN_NAME) final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcVerifiableCredentialJwtVcJsonLdEncoder(oidcConfigurationContext);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcVerifiableCredentialEncoderFactory")
    @Bean
    public OidcVerifiableCredentialEncoderFactory oidcVerifiableCredentialEncoderFactory(
        final List<OidcVerifiableCredentialEncoder> encoders,
        final CasConfigurationProperties casProperties) {
        val factory = new OidcVerifiableCredentialEncoderFactory(casProperties);
        encoders.forEach(factory::register);
        return factory;
    }

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
    public OidcVerifiableCredentialIssuerMetadataController oidcCredentialIssuerMetadataController(
        @Qualifier(OidcConfigurationContext.BEAN_NAME) final OidcConfigurationContext oidcConfigurationContext,
        @Qualifier("oidcCredentialIssuerMetadataService") final OidcCredentialIssuerMetadataService metadataService) {
        return new OidcVerifiableCredentialIssuerMetadataController(oidcConfigurationContext, metadataService);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcCredentialTypeMetadataController")
    @Bean
    public OidcVerifiableCredentialTypeMetadataController oidcCredentialTypeMetadataController(
        @Qualifier(OidcConfigurationContext.BEAN_NAME) final OidcConfigurationContext oidcConfigurationContext,
        @Qualifier("oidcCredentialIssuerMetadataService") final OidcCredentialIssuerMetadataService metadataService) {
        return new OidcVerifiableCredentialTypeMetadataController(oidcConfigurationContext, metadataService);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcVerifiableCredentialProofValidator")
    @Bean
    public OidcVerifiableCredentialProofValidator oidcVerifiableCredentialProofValidator(
        @Qualifier(OidcVerifiableCredentialNonceService.BEAN_NAME)
        final OidcVerifiableCredentialNonceService oidcVerifiableCredentialNonceService,
        final CasConfigurationProperties casProperties) {
        return new OidcVerifiableCredentialJwtProofValidator(casProperties, oidcVerifiableCredentialNonceService);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcVerifiableCredentialIssuerService")
    @Bean
    public OidcVerifiableCredentialIssuerService oidcVerifiableCredentialIssuerService(
        @Qualifier("oidcVerifiableCredentialEncoderFactory")
        final OidcVerifiableCredentialEncoderFactory oidcVerifiableCredentialEncoderFactory,
        @Qualifier("oidcVerifiableCredentialProofValidator")
        final OidcVerifiableCredentialProofValidator oidcVerifiableCredentialProofValidator) {
        return new OidcDefaultVerifiableCredentialIssuerService(
            oidcVerifiableCredentialProofValidator, oidcVerifiableCredentialEncoderFactory);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcCredentialEndpointController")
    @Bean
    public OidcVerifiableCredentialEndpointController oidcCredentialEndpointController(
        @Qualifier(OidcVerifiableCredentialNonceService.BEAN_NAME)
        final OidcVerifiableCredentialNonceService oidcVerifiableCredentialNonceService,
        @Qualifier(OidcConfigurationContext.BEAN_NAME)
        final OidcConfigurationContext oidcConfigurationContext,
        @Qualifier("oidcVerifiableCredentialIssuerService")
        final OidcVerifiableCredentialIssuerService oidcVerifiableCredentialIssuerService) {
        return new OidcVerifiableCredentialEndpointController(
            oidcConfigurationContext, oidcVerifiableCredentialIssuerService, oidcVerifiableCredentialNonceService);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = OidcVerifiableCredentialNonceService.BEAN_NAME)
    @Bean
    public OidcVerifiableCredentialNonceService oidcVerifiableCredentialNonceService(
        @Qualifier(OidcConfigurationContext.BEAN_NAME) final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcVerifiableCredentialDefaultNonceService(oidcConfigurationContext);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcVerifiableCredentialNonceEndpointController")
    @Bean
    public OidcVerifiableCredentialNonceEndpointController oidcVerifiableCredentialNonceEndpointController(
        @Qualifier(OidcVerifiableCredentialNonceService.BEAN_NAME) final OidcVerifiableCredentialNonceService oidcVerifiableCredentialNonceService,
        @Qualifier(OidcConfigurationContext.BEAN_NAME) final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcVerifiableCredentialNonceEndpointController(oidcConfigurationContext, oidcVerifiableCredentialNonceService);
    }
}
