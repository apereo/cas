package org.apereo.cas.config;

import net.shibboleth.ext.spring.resource.ResourceHelper;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.logout.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlIdPSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.support.saml.services.idp.metadata.cache.ChainingMetadataResolverCacheLoader;
import org.apereo.cas.support.saml.services.idp.metadata.cache.DefaultSamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.metadata.SamlIdpMetadataAndCertificatesGenerationService;
import org.apereo.cas.support.saml.web.idp.metadata.SamlMetadataController;
import org.apereo.cas.support.saml.web.idp.metadata.TemplatedMetadataAndCertificatesGenerationService;
import org.apereo.cas.support.saml.web.idp.profile.ECPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.IdPInitiatedProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthnContextClassRefBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.DefaultAuthnContextClassRefBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlAssertionBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlAttributeStatementBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlAuthNStatementBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlConditionsBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlNameIdBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlSubjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlAttributeEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSignatureValidator;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidator;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSaml2ResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlSoap11FaultResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlSoap11ResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.slo.SLOPostProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.slo.SLORedirectProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOPostProfileCallbackHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOPostProfileHandlerController;
import org.apereo.cas.util.http.HttpClient;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.ResourceBackedMetadataResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.ecp.Response;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.ui.velocity.VelocityEngineFactory;

import javax.net.ssl.HostnameVerifier;

/**
 * The {@link SamlIdPConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("samlIdPConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdPConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("hostnameVerifier")
    private HostnameVerifier hostnameVerifier;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    @Qualifier("shibboleth.VelocityEngine")
    private VelocityEngineFactory velocityEngineFactory;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Bean
    public SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder() {
        return new SamlIdPSingleLogoutServiceLogoutUrlBuilder(servicesManager, defaultSamlRegisteredServiceCachingMetadataResolver());
    }

    @ConditionalOnMissingBean(name = "chainingMetadataResolverCacheLoader")
    @Bean
    @RefreshScope
    public ChainingMetadataResolverCacheLoader chainingMetadataResolverCacheLoader() {
        return new ChainingMetadataResolverCacheLoader(
                openSamlConfigBean, httpClient
        );
    }

    @ConditionalOnMissingBean(name = "defaultSamlRegisteredServiceCachingMetadataResolver")
    @Bean
    @RefreshScope
    public SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver() {
        return new DefaultSamlRegisteredServiceCachingMetadataResolver(
                casProperties.getAuthn().getSamlIdp().getMetadata().getCacheExpirationMinutes(),
                chainingMetadataResolverCacheLoader()
        );
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<org.opensaml.saml.saml2.core.Response> samlProfileSamlResponseBuilder() {
        return new SamlProfileSaml2ResponseBuilder(
                openSamlConfigBean,
                samlObjectSigner(),
                velocityEngineFactory,
                samlProfileSamlAssertionBuilder(),
                samlObjectEncrypter());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlSubjectBuilder")
    @Bean
    @RefreshScope
    public SamlProfileSamlSubjectBuilder samlProfileSamlSubjectBuilder() {
        return new SamlProfileSamlSubjectBuilder(openSamlConfigBean, samlProfileSamlNameIdBuilder(),
                casProperties.getAuthn().getSamlIdp().getResponse().getSkewAllowance());
    }

    @ConditionalOnMissingBean(name = "samlObjectEncrypter")
    @Bean
    @RefreshScope
    public SamlObjectEncrypter samlObjectEncrypter() {
        final SamlIdPProperties.Algorithms algs = casProperties.getAuthn().getSamlIdp().getAlgs();
        return new SamlObjectEncrypter(algs.getOverrideDataEncryptionAlgorithms(),
                algs.getOverrideKeyEncryptionAlgorithms(),
                algs.getOverrideBlackListedEncryptionAlgorithms(),
                algs.getOverrideWhiteListedAlgorithms());
    }

    @ConditionalOnMissingBean(name = "samlObjectSigner")
    @Bean
    @RefreshScope
    public BaseSamlObjectSigner samlObjectSigner() {
        final SamlIdPProperties.Algorithms algs = casProperties.getAuthn().getSamlIdp().getAlgs();
        return new BaseSamlObjectSigner(
                algs.getOverrideSignatureReferenceDigestMethods(),
                algs.getOverrideSignatureAlgorithms(),
                algs.getOverrideBlackListedSignatureSigningAlgorithms(),
                algs.getOverrideWhiteListedSignatureSigningAlgorithms());
    }

    @ConditionalOnMissingBean(name = "shibbolethIdpMetadataAndCertificatesGenerationService")
    @Bean
    public SamlIdpMetadataAndCertificatesGenerationService shibbolethIdpMetadataAndCertificatesGenerationService() {
        return new TemplatedMetadataAndCertificatesGenerationService();
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlSoap11FaultResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Response> samlProfileSamlSoap11FaultResponseBuilder() {
        return new SamlProfileSamlSoap11FaultResponseBuilder(
                openSamlConfigBean,
                samlObjectSigner(),
                velocityEngineFactory,
                samlProfileSamlAssertionBuilder(),
                samlProfileSamlResponseBuilder(),
                samlObjectEncrypter());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlSoap11ResponseBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Response> samlProfileSamlSoap11ResponseBuilder() {
        return new SamlProfileSamlSoap11ResponseBuilder(
                openSamlConfigBean,
                samlObjectSigner(),
                velocityEngineFactory,
                samlProfileSamlAssertionBuilder(),
                samlProfileSamlResponseBuilder(),
                samlObjectEncrypter());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlNameIdBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<NameID> samlProfileSamlNameIdBuilder() {
        return new SamlProfileSamlNameIdBuilder(openSamlConfigBean);
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlConditionsBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Conditions> samlProfileSamlConditionsBuilder() {
        return new SamlProfileSamlConditionsBuilder(openSamlConfigBean);
    }

    @ConditionalOnMissingBean(name = "defaultAuthnContextClassRefBuilder")
    @Bean
    @RefreshScope
    public AuthnContextClassRefBuilder defaultAuthnContextClassRefBuilder() {
        return new DefaultAuthnContextClassRefBuilder();
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlAssertionBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder() {
        return new SamlProfileSamlAssertionBuilder(openSamlConfigBean,
                samlProfileSamlAuthNStatementBuilder(),
                samlProfileSamlAttributeStatementBuilder(),
                samlProfileSamlSubjectBuilder(),
                samlProfileSamlConditionsBuilder(),
                samlObjectSigner());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlAuthNStatementBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<AuthnStatement> samlProfileSamlAuthNStatementBuilder() {
        return new SamlProfileSamlAuthNStatementBuilder(openSamlConfigBean, defaultAuthnContextClassRefBuilder());
    }

    @ConditionalOnMissingBean(name = "samlProfileSamlAttributeStatementBuilder")
    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<AttributeStatement> samlProfileSamlAttributeStatementBuilder() {
        return new SamlProfileSamlAttributeStatementBuilder(openSamlConfigBean, new SamlAttributeEncoder());
    }

    @ConditionalOnMissingBean(name = "samlIdPObjectSignatureValidator")
    @Bean
    public SamlObjectSignatureValidator samlIdPObjectSignatureValidator() {
        final SamlIdPProperties.Algorithms algs = casProperties.getAuthn().getSamlIdp().getAlgs();
        return new SamlIdPObjectSignatureValidator(
                algs.getOverrideSignatureReferenceDigestMethods(),
                algs.getOverrideSignatureAlgorithms(),
                algs.getOverrideBlackListedSignatureSigningAlgorithms(),
                algs.getOverrideWhiteListedSignatureSigningAlgorithms(),
                casSamlIdPMetadataResolver()
        );
    }

    @ConditionalOnMissingBean(name = "samlObjectSignatureValidator")
    @Bean
    public SamlObjectSignatureValidator samlObjectSignatureValidator() {
        final SamlIdPProperties.Algorithms algs = casProperties.getAuthn().getSamlIdp().getAlgs();
        return new SamlObjectSignatureValidator(
                algs.getOverrideSignatureReferenceDigestMethods(),
                algs.getOverrideSignatureAlgorithms(),
                algs.getOverrideBlackListedSignatureSigningAlgorithms(),
                algs.getOverrideWhiteListedSignatureSigningAlgorithms()
        );
    }

    @Bean
    @RefreshScope
    public SSOPostProfileHandlerController ssoPostProfileHandlerController() {
        return new SSOPostProfileHandlerController(
                samlObjectSigner(),
                openSamlConfigBean.getParserPool(),
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver(),
                openSamlConfigBean,
                samlProfileSamlResponseBuilder(),
                casProperties.getAuthn().getSamlIdp().getAuthenticationContextClassMappings(),
                casProperties.getServer().getPrefix(),
                casProperties.getServer().getName(),
                casProperties.getAuthn().getMfa().getRequestParameter(),
                casProperties.getServer().getLoginUrl(),
                casProperties.getServer().getLogoutUrl(),
                casProperties.getAuthn().getSamlIdp().getLogout().isForceSignedLogoutRequests(),
                casProperties.getAuthn().getSamlIdp().getLogout().isSingleLogoutCallbacksDisabled(),
                samlObjectSignatureValidator());
    }

    @Bean
    @RefreshScope
    public SLORedirectProfileHandlerController sloRedirectProfileHandlerController() {
        final SamlIdPProperties idp = casProperties.getAuthn().getSamlIdp();
        return new SLORedirectProfileHandlerController(
                samlObjectSigner(),
                openSamlConfigBean.getParserPool(),
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver(),
                openSamlConfigBean,
                samlProfileSamlResponseBuilder(),
                casProperties.getAuthn().getSamlIdp().getAuthenticationContextClassMappings(),
                casProperties.getServer().getPrefix(),
                casProperties.getServer().getName(),
                casProperties.getAuthn().getMfa().getRequestParameter(),
                casProperties.getServer().getLoginUrl(),
                casProperties.getServer().getLogoutUrl(),
                idp.getLogout().isForceSignedLogoutRequests(),
                idp.getLogout().isSingleLogoutCallbacksDisabled(),
                samlObjectSignatureValidator());
    }

    @Bean
    @RefreshScope
    public SLOPostProfileHandlerController sloPostProfileHandlerController() {
        final SamlIdPProperties idp = casProperties.getAuthn().getSamlIdp();
        return new SLOPostProfileHandlerController(
                samlObjectSigner(),
                openSamlConfigBean.getParserPool(),
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver(),
                openSamlConfigBean,
                samlProfileSamlResponseBuilder(),
                casProperties.getAuthn().getSamlIdp().getAuthenticationContextClassMappings(),
                casProperties.getServer().getPrefix(),
                casProperties.getServer().getName(),
                casProperties.getAuthn().getMfa().getRequestParameter(),
                casProperties.getServer().getLoginUrl(),
                casProperties.getServer().getLogoutUrl(),
                idp.getLogout().isForceSignedLogoutRequests(),
                idp.getLogout().isSingleLogoutCallbacksDisabled(),
                samlObjectSignatureValidator());
    }

    @Bean
    @RefreshScope
    public IdPInitiatedProfileHandlerController idPInitiatedSamlProfileHandlerController() {
        final SamlIdPProperties idp = casProperties.getAuthn().getSamlIdp();
        return new IdPInitiatedProfileHandlerController(
                samlObjectSigner(),
                openSamlConfigBean.getParserPool(),
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver(),
                openSamlConfigBean,
                samlProfileSamlResponseBuilder(),
                idp.getAuthenticationContextClassMappings(),
                casProperties.getServer().getPrefix(),
                casProperties.getServer().getName(),
                casProperties.getAuthn().getMfa().getRequestParameter(),
                casProperties.getServer().getLoginUrl(),
                casProperties.getServer().getLogoutUrl(),
                idp.getLogout().isForceSignedLogoutRequests(),
                idp.getLogout().isSingleLogoutCallbacksDisabled(),
                samlIdPObjectSignatureValidator());
    }

    @Bean
    @RefreshScope
    public SSOPostProfileCallbackHandlerController ssoPostProfileCallbackHandlerController() {
        final SamlIdPProperties idp = casProperties.getAuthn().getSamlIdp();
        return new SSOPostProfileCallbackHandlerController(
                samlObjectSigner(),
                openSamlConfigBean.getParserPool(),
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver(),
                openSamlConfigBean,
                samlProfileSamlResponseBuilder(),
                idp.getAuthenticationContextClassMappings(),
                casProperties.getServer().getPrefix(),
                casProperties.getServer().getName(),
                casProperties.getAuthn().getMfa().getRequestParameter(),
                casProperties.getServer().getLoginUrl(),
                casProperties.getServer().getLogoutUrl(),
                idp.getLogout().isForceSignedLogoutRequests(),
                idp.getLogout().isSingleLogoutCallbacksDisabled(),
                samlObjectSignatureValidator(),
                this.hostnameVerifier);
    }

    @Bean
    @RefreshScope
    public ECPProfileHandlerController ecpProfileHandlerController() {
        final SamlIdPProperties idp = casProperties.getAuthn().getSamlIdp();
        return new ECPProfileHandlerController(samlObjectSigner(),
                openSamlConfigBean.getParserPool(),
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver(),
                openSamlConfigBean,
                samlProfileSamlSoap11ResponseBuilder(),
                samlProfileSamlSoap11FaultResponseBuilder(),
                idp.getAuthenticationContextClassMappings(),
                casProperties.getServer().getPrefix(),
                casProperties.getServer().getName(),
                casProperties.getAuthn().getMfa().getRequestParameter(),
                casProperties.getServer().getLoginUrl(),
                casProperties.getServer().getLogoutUrl(),
                idp.getLogout().isForceSignedLogoutRequests(),
                idp.getLogout().isSingleLogoutCallbacksDisabled(),
                samlObjectSignatureValidator());
    }

    @Bean
    public MetadataResolver casSamlIdPMetadataResolver() {
        try {
            final SamlIdPProperties idp = casProperties.getAuthn().getSamlIdp();
            final ResourceBackedMetadataResolver resolver = new ResourceBackedMetadataResolver(
                    ResourceHelper.of(new FileSystemResource(idp.getMetadata().getMetadataFile())));
            resolver.setParserPool(this.openSamlConfigBean.getParserPool());
            resolver.setFailFastInitialization(idp.getMetadata().isFailFast());
            resolver.setRequireValidMetadata(idp.getMetadata().isRequireValidMetadata());
            resolver.setId(idp.getEntityId());
            resolver.initialize();
            return resolver;
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Bean
    @RefreshScope
    public SamlMetadataController samlMetadataController() {
        return new SamlMetadataController(shibbolethIdpMetadataAndCertificatesGenerationService());
    }
}
