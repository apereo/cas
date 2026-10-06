package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.vc.issuer.OidcDefaultVerifiableCredentialIssuerService;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialIssuerService;
import org.apereo.cas.oidc.vc.issuer.enc.OidcVerifiableCredentialDcSdJwtEncoder;
import org.apereo.cas.oidc.vc.issuer.enc.OidcVerifiableCredentialEncoder;
import org.apereo.cas.oidc.vc.issuer.enc.OidcVerifiableCredentialEncoderFactory;
import org.apereo.cas.oidc.vc.issuer.enc.OidcVerifiableCredentialJwtVcJsonEncoder;
import org.apereo.cas.oidc.vc.issuer.enc.OidcVerifiableCredentialJwtVcJsonLdEncoder;
import org.apereo.cas.oidc.vc.issuer.encryption.OidcVerifiableCredentialDefaultEncryptionService;
import org.apereo.cas.oidc.vc.issuer.encryption.OidcVerifiableCredentialEncryptionService;
import org.apereo.cas.oidc.vc.issuer.metadata.OidcCredentialIssuerMetadataService;
import org.apereo.cas.oidc.vc.issuer.nonce.OidcVerifiableCredentialDefaultNonceService;
import org.apereo.cas.oidc.vc.issuer.nonce.OidcVerifiableCredentialNonceService;
import org.apereo.cas.oidc.vc.issuer.notification.OidcVerifiableCredentialDefaultNotificationService;
import org.apereo.cas.oidc.vc.issuer.notification.OidcVerifiableCredentialNotificationService;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialJwtProofValidator;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialKeyAttestationValidator;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator;
import org.apereo.cas.oidc.vc.issuer.status.OidcVerifiableCredentialDefaultStatusListService;
import org.apereo.cas.oidc.vc.issuer.status.OidcVerifiableCredentialStatusEndpoint;
import org.apereo.cas.oidc.vc.issuer.status.OidcVerifiableCredentialStatusListService;
import org.apereo.cas.oidc.vc.issuer.web.OidcVerifiableCredentialEndpointController;
import org.apereo.cas.oidc.vc.issuer.web.OidcVerifiableCredentialIssuerMetadataController;
import org.apereo.cas.oidc.vc.issuer.web.OidcVerifiableCredentialNonceEndpointController;
import org.apereo.cas.oidc.vc.issuer.web.OidcVerifiableCredentialStatusListEndpointController;
import org.apereo.cas.oidc.vc.issuer.web.OidcVerifiableCredentialTypeMetadataController;
import org.apereo.cas.oidc.vc.token.OidcVerifiableCredentialsAccessTokenGeneratorCustomizer;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.val;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.apereo.inspektr.audit.spi.support.ShortenedReturnValueAsStringAuditResourceResolver;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
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
        @Qualifier(OidcConfigurationContext.BEAN_NAME) final OidcConfigurationContext oidcConfigurationContext,
        @Qualifier(OidcVerifiableCredentialStatusListService.BEAN_NAME)
        final OidcVerifiableCredentialStatusListService oidcVerifiableCredentialStatusListService) {
        return new OidcVerifiableCredentialDcSdJwtEncoder(oidcConfigurationContext, oidcVerifiableCredentialStatusListService);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = OidcVerifiableCredentialStatusListService.BEAN_NAME)
    @Bean
    public OidcVerifiableCredentialStatusListService oidcVerifiableCredentialStatusListService(
        @Qualifier(OidcConfigurationContext.BEAN_NAME) final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcVerifiableCredentialDefaultStatusListService(oidcConfigurationContext);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcVerifiableCredentialStatusListEndpointController")
    @Bean
    public OidcVerifiableCredentialStatusListEndpointController oidcVerifiableCredentialStatusListEndpointController(
        @Qualifier(OidcVerifiableCredentialStatusListService.BEAN_NAME)
        final OidcVerifiableCredentialStatusListService oidcVerifiableCredentialStatusListService,
        @Qualifier(OidcConfigurationContext.BEAN_NAME) final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcVerifiableCredentialStatusListEndpointController(oidcConfigurationContext, oidcVerifiableCredentialStatusListService);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OidcVerifiableCredentialStatusEndpoint oidcVerifiableCredentialStatusEndpoint(
        @Qualifier(OidcVerifiableCredentialStatusListService.BEAN_NAME)
        final ObjectProvider<OidcVerifiableCredentialStatusListService> oidcVerifiableCredentialStatusListService,
        final CasConfigurationProperties casProperties) {
        return new OidcVerifiableCredentialStatusEndpoint(casProperties, oidcVerifiableCredentialStatusListService);
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
        @Qualifier(OidcVerifiableCredentialEncryptionService.BEAN_NAME)
        final OidcVerifiableCredentialEncryptionService oidcVerifiableCredentialEncryptionService,
        final CasConfigurationProperties casProperties) {
        return new OidcCredentialIssuerMetadataService(casProperties, oidcVerifiableCredentialEncryptionService);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = OidcVerifiableCredentialEncryptionService.BEAN_NAME)
    @Bean
    public OidcVerifiableCredentialEncryptionService oidcVerifiableCredentialEncryptionService(
        @Qualifier(OidcConfigurationContext.BEAN_NAME) final OidcConfigurationContext oidcConfigurationContext,
        @Qualifier("oidcDefaultJsonWebKeystoreCache")
        final LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> oidcDefaultJsonWebKeystoreCache) {
        return new OidcVerifiableCredentialDefaultEncryptionService(oidcConfigurationContext, oidcDefaultJsonWebKeystoreCache);
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
        return new OidcVerifiableCredentialJwtProofValidator(casProperties, oidcVerifiableCredentialNonceService,
            new OidcVerifiableCredentialKeyAttestationValidator(casProperties));
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
        @Qualifier(OidcConfigurationContext.BEAN_NAME)
        final OidcConfigurationContext oidcConfigurationContext,
        @Qualifier("oidcVerifiableCredentialIssuerService")
        final OidcVerifiableCredentialIssuerService oidcVerifiableCredentialIssuerService,
        @Qualifier(OidcVerifiableCredentialNotificationService.BEAN_NAME)
        final OidcVerifiableCredentialNotificationService oidcVerifiableCredentialNotificationService,
        @Qualifier(OidcVerifiableCredentialEncryptionService.BEAN_NAME)
        final OidcVerifiableCredentialEncryptionService oidcVerifiableCredentialEncryptionService) {
        return new OidcVerifiableCredentialEndpointController(oidcConfigurationContext, oidcVerifiableCredentialIssuerService,
            oidcVerifiableCredentialNotificationService, oidcVerifiableCredentialEncryptionService);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = OidcVerifiableCredentialNotificationService.BEAN_NAME)
    @Bean
    public OidcVerifiableCredentialNotificationService oidcVerifiableCredentialNotificationService(
        @Qualifier(OidcConfigurationContext.BEAN_NAME) final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcVerifiableCredentialDefaultNotificationService(oidcConfigurationContext);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcVerifiableCredentialAuditTrailRecordResolutionPlanConfigurer")
    public AuditTrailRecordResolutionPlanConfigurer oidcVerifiableCredentialAuditTrailRecordResolutionPlanConfigurer() {
        return plan -> {
            plan.registerAuditActionResolver(AuditActionResolvers.OIDC_VERIFIABLE_CREDENTIAL_NOTIFICATION_ACTION_RESOLVER,
                new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_SUCCESS, AuditTrailConstants.AUDIT_ACTION_POSTFIX_FAILED));
            plan.registerAuditResourceResolver(AuditResourceResolvers.OIDC_VERIFIABLE_CREDENTIAL_NOTIFICATION_RESOURCE_RESOLVER,
                new ShortenedReturnValueAsStringAuditResourceResolver());
        };
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
        @Qualifier(OidcVerifiableCredentialNonceService.BEAN_NAME)
        final OidcVerifiableCredentialNonceService oidcVerifiableCredentialNonceService,
        @Qualifier(OidcConfigurationContext.BEAN_NAME)
        final OidcConfigurationContext oidcConfigurationContext) {
        return new OidcVerifiableCredentialNonceEndpointController(oidcConfigurationContext, oidcVerifiableCredentialNonceService);
    }
    
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oidcVerifiableCredentialsAccessTokenGeneratorCustomizer")
    public OidcVerifiableCredentialsAccessTokenGeneratorCustomizer oidcVerifiableCredentialsAccessTokenGeneratorCustomizer(
        final CasConfigurationProperties casProperties) {
        return new OidcVerifiableCredentialsAccessTokenGeneratorCustomizer(casProperties);
    }
}
