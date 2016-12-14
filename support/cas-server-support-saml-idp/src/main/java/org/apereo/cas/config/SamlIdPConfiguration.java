package org.apereo.cas.config;

import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlIdPEntityIdAuthenticationRequestServiceSelectionStrategy;
import org.apereo.cas.support.saml.services.SamlIdPSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.support.saml.services.idp.metadata.cache.ChainingMetadataResolverCacheLoader;
import org.apereo.cas.support.saml.services.idp.metadata.cache.DefaultSamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.flow.SamlIdPMetadataUIAction;
import org.apereo.cas.support.saml.web.flow.SamlIdPMetadataUIWebflowConfigurer;
import org.apereo.cas.support.saml.web.idp.metadata.SamlIdpMetadataAndCertificatesGenerationService;
import org.apereo.cas.support.saml.web.idp.metadata.SamlMetadataController;
import org.apereo.cas.support.saml.web.idp.metadata.ShibbolethIdpMetadataAndCertificatesGenerationService;
import org.apereo.cas.support.saml.web.idp.profile.ECPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.IdPInitiatedProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SLOPostProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SSOPostProfileCallbackHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SSOPostProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthnContextClassRefBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.DefaultAuthnContextClassRefBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlAssertionBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlAttributeStatementBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlAuthNStatementBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlConditionsBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlNameIdBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.BaseSamlProfileSamlResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.response.SamlProfileSamlSoap11ResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlSubjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlAttributeEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectEncrypter;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSigner;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.ecp.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.velocity.VelocityEngineFactory;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

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
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    @Qualifier("authenticationRequestServiceSelectionStrategies")
    private List<AuthenticationRequestServiceSelectionStrategy> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("shibboleth.VelocityEngine")
    private VelocityEngineFactory velocityEngineFactory;

    @Autowired
    @Qualifier("shibboleth.ParserPool")
    private BasicParserPool parserPool;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired(required = false)
    @Qualifier("authenticationContextClassMappings")
    private Map authenticationContextClassMappings;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired(required = false)
    @Qualifier("overrideDataEncryptionAlgorithms")
    private List overrideDataEncryptionAlgorithms;

    @Autowired(required = false)
    @Qualifier("overrideKeyEncryptionAlgorithms")
    private List overrideKeyEncryptionAlgorithms;

    @Autowired(required = false)
    @Qualifier("overrideBlackListedEncryptionAlgorithms")
    private List overrideBlackListedEncryptionAlgorithms;

    @Autowired(required = false)
    @Qualifier("overrideWhiteListedEncryptionAlgorithms")
    private List overrideWhiteListedAlgorithms;

    @Autowired(required = false)
    @Qualifier("overrideSignatureReferenceDigestMethods")
    private List overrideSignatureReferenceDigestMethods;

    @Autowired(required = false)
    @Qualifier("overrideSignatureAlgorithms")
    private List overrideSignatureAlgorithms;

    @Autowired(required = false)
    @Qualifier("overrideBlackListedSignatureAlgorithms")
    private List overrideBlackListedSignatureSigningAlgorithms;

    @Autowired(required = false)
    @Qualifier("overrideWhiteListedSignatureSigningAlgorithms")
    private List overrideWhiteListedSignatureSigningAlgorithms;

    @Autowired(required = false)
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired(required = false)
    private FlowBuilderServices flowBuilderServices;

    @ConditionalOnMissingBean(name = "samlIdPMetadataUIWebConfigurer")
    @Bean
    public CasWebflowConfigurer samlIdPMetadataUIWebConfigurer() {
        final SamlIdPMetadataUIWebflowConfigurer w = new SamlIdPMetadataUIWebflowConfigurer();
        w.setSamlMetadataUIParserAction(samlIdPMetadataUIParserAction());
        w.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
        w.setFlowBuilderServices(flowBuilderServices);
        return w;
    }

    @Bean
    public Action samlIdPMetadataUIParserAction() {
        return new SamlIdPMetadataUIAction(
                servicesManager,
                defaultSamlRegisteredServiceCachingMetadataResolver(),
                samlIdPEntityIdValidationServiceSelectionStrategy());
    }

    @PostConstruct
    public void init() {
        this.authenticationRequestServiceSelectionStrategies.add(0, samlIdPEntityIdValidationServiceSelectionStrategy());
    }

    /**
     * Saml id p single logout service logout url builder saml id p single logout service logout url builder.
     *
     * @return the saml idp single logout service logout url builder
     */
    @Bean(name = {"defaultSingleLogoutServiceLogoutUrlBuilder", "samlIdPSingleLogoutServiceLogoutUrlBuilder"})
    public SamlIdPSingleLogoutServiceLogoutUrlBuilder samlIdPSingleLogoutServiceLogoutUrlBuilder() {
        final SamlIdPSingleLogoutServiceLogoutUrlBuilder b = new SamlIdPSingleLogoutServiceLogoutUrlBuilder();
        b.setSamlRegisteredServiceCachingMetadataResolver(defaultSamlRegisteredServiceCachingMetadataResolver());
        b.setServicesManager(servicesManager);
        return b;
    }

    @Bean
    public AuthenticationRequestServiceSelectionStrategy samlIdPEntityIdValidationServiceSelectionStrategy() {
        final SamlIdPEntityIdAuthenticationRequestServiceSelectionStrategy s = new SamlIdPEntityIdAuthenticationRequestServiceSelectionStrategy();
        s.setWebApplicationServiceFactory(webApplicationServiceFactory);
        return s;
    }

    @Bean
    @RefreshScope
    public ChainingMetadataResolverCacheLoader chainingMetadataResolverCacheLoader() {
        final ChainingMetadataResolverCacheLoader c = new ChainingMetadataResolverCacheLoader();

        final SamlIdPProperties.Metadata md = casProperties.getAuthn().getSamlIdp().getMetadata();
        c.setFailFastInitialization(md.isFailFast());
        c.setMetadataCacheExpirationMinutes(md.getCacheExpirationMinutes());
        c.setRequireValidMetadata(md.isRequireValidMetadata());
        c.setConfigBean(this.openSamlConfigBean);
        c.setHttpClient(this.httpClient);
        c.setBasicAuthnUsername(md.getBasicAuthnUsername());
        c.setBasicAuthnPassword(md.getBasicAuthnPassword());
        c.setSupportedContentTypes(md.getSupportedContentTypes());
        c.setMetadataLocation(casProperties.getAuthn().getSamlIdp().getMetadata().getLocation());
        return c;
    }

    @Bean
    @RefreshScope
    public SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver() {
        final DefaultSamlRegisteredServiceCachingMetadataResolver r = new DefaultSamlRegisteredServiceCachingMetadataResolver();
        r.setChainingMetadataResolverCacheLoader(chainingMetadataResolverCacheLoader());
        r.setMetadataCacheExpirationMinutes(casProperties.getAuthn().getSamlIdp().getMetadata().getCacheExpirationMinutes());
        r.setChainingMetadataResolverCacheLoader(chainingMetadataResolverCacheLoader());
        return r;
    }

    @Bean
    @RefreshScope
    public BaseSamlProfileSamlResponseBuilder samlProfileSamlResponseBuilder() {
        final BaseSamlProfileSamlResponseBuilder b = new BaseSamlProfileSamlResponseBuilder();
        b.setConfigBean(openSamlConfigBean);
        b.setSamlObjectEncrypter(samlObjectEncrypter());
        b.setSamlProfileSamlAssertionBuilder(samlProfileSamlAssertionBuilder());
        b.setVelocityEngineFactory(velocityEngineFactory);
        b.setSamlObjectSigner(samlObjectSigner());
        return b;
    }


    @Bean
    @RefreshScope
    public SamlProfileSamlSubjectBuilder samlProfileSamlSubjectBuilder() {
        final SamlProfileSamlSubjectBuilder b = new SamlProfileSamlSubjectBuilder();
        b.setConfigBean(openSamlConfigBean);
        b.setSkewAllowance(casProperties.getAuthn().getSamlIdp().getResponse().getSkewAllowance());
        b.setSsoPostProfileSamlNameIdBuilder(samlProfileSamlNameIdBuilder());
        return b;
    }

    @Bean
    @RefreshScope
    public SamlObjectEncrypter samlObjectEncrypter() {
        final SamlObjectEncrypter e = new SamlObjectEncrypter();
        e.setOverrideBlackListedEncryptionAlgorithms(overrideBlackListedEncryptionAlgorithms);
        e.setOverrideDataEncryptionAlgorithms(overrideDataEncryptionAlgorithms);
        e.setOverrideKeyEncryptionAlgorithms(overrideKeyEncryptionAlgorithms);
        e.setOverrideWhiteListedAlgorithms(overrideWhiteListedAlgorithms);
        return e;
    }

    @Bean
    @RefreshScope
    public SamlObjectSigner samlObjectSigner() {
        final SamlObjectSigner s = new SamlObjectSigner();
        s.setOverrideBlackListedSignatureAlgorithms(overrideBlackListedSignatureSigningAlgorithms);
        s.setOverrideSignatureAlgorithms(overrideSignatureAlgorithms);
        s.setOverrideSignatureReferenceDigestMethods(overrideSignatureReferenceDigestMethods);
        s.setOverrideWhiteListedAlgorithms(overrideWhiteListedSignatureSigningAlgorithms);
        return s;
    }

    @Bean
    public SamlIdpMetadataAndCertificatesGenerationService shibbolethIdpMetadataAndCertificatesGenerationService() {
        return new ShibbolethIdpMetadataAndCertificatesGenerationService();
    }

    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Response> samlProfileSamlSoap11ResponseBuilder() {
        final SamlProfileSamlSoap11ResponseBuilder b = new SamlProfileSamlSoap11ResponseBuilder();
        b.setConfigBean(openSamlConfigBean);
        return b;
    }

    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<NameID> samlProfileSamlNameIdBuilder() {
        final SamlProfileSamlNameIdBuilder b = new SamlProfileSamlNameIdBuilder();
        b.setConfigBean(openSamlConfigBean);
        return b;
    }

    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Conditions> samlProfileSamlConditionsBuilder() {
        final SamlProfileSamlConditionsBuilder b = new SamlProfileSamlConditionsBuilder();
        b.setConfigBean(openSamlConfigBean);
        return b;
    }

    @Bean
    @RefreshScope
    public AuthnContextClassRefBuilder defaultAuthnContextClassRefBuilder() {
        return new DefaultAuthnContextClassRefBuilder();
    }

    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder() {
        final SamlProfileSamlAssertionBuilder b = new SamlProfileSamlAssertionBuilder();

        b.setConfigBean(openSamlConfigBean);
        b.setSamlObjectSigner(samlObjectSigner());
        b.setSamlProfileSamlAttributeStatementBuilder(samlProfileSamlAttributeStatementBuilder());
        b.setSamlProfileSamlAuthNStatementBuilder(samlProfileSamlAuthNStatementBuilder());
        b.setSamlProfileSamlConditionsBuilder(samlProfileSamlConditionsBuilder());
        b.setSamlProfileSamlSubjectBuilder(samlProfileSamlSubjectBuilder());
        return b;
    }

    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<AuthnStatement> samlProfileSamlAuthNStatementBuilder() {
        final SamlProfileSamlAuthNStatementBuilder b = new SamlProfileSamlAuthNStatementBuilder();
        b.setConfigBean(openSamlConfigBean);
        b.setAuthnContextClassRefBuilder(defaultAuthnContextClassRefBuilder());
        return b;
    }

    @Bean
    @RefreshScope
    public SamlProfileObjectBuilder<AttributeStatement> samlProfileSamlAttributeStatementBuilder() {
        final SamlProfileSamlAttributeStatementBuilder b = new SamlProfileSamlAttributeStatementBuilder();
        b.setSamlAttributeEncoder(new SamlAttributeEncoder());
        b.setConfigBean(openSamlConfigBean);
        return b;
    }

    @Bean
    @RefreshScope
    public SSOPostProfileHandlerController ssoPostProfileHandlerController() {
        final SSOPostProfileHandlerController c = new SSOPostProfileHandlerController(
                samlObjectSigner(),
                parserPool,
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver(),
                openSamlConfigBean,
                samlProfileSamlResponseBuilder(),
                authenticationContextClassMappings,
                casProperties.getServer().getPrefix(),
                casProperties.getServer().getName(),
                casProperties.getAuthn().getMfa().getRequestParameter(),
                casProperties.getServer().getLoginUrl(),
                casProperties.getServer().getLogoutUrl(),
                casProperties.getAuthn().getSamlIdp().getLogout().isForceSignedLogoutRequests(),
                casProperties.getAuthn().getSamlIdp().getLogout().isSingleLogoutCallbacksDisabled());
        return c;
    }

    @Bean
    @RefreshScope
    public SLOPostProfileHandlerController sloPostProfileHandlerController() {
        final SLOPostProfileHandlerController c = new SLOPostProfileHandlerController(
                samlObjectSigner(),
                parserPool,
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver(),
                openSamlConfigBean,
                samlProfileSamlResponseBuilder(),
                authenticationContextClassMappings,
                casProperties.getServer().getPrefix(),
                casProperties.getServer().getName(),
                casProperties.getAuthn().getMfa().getRequestParameter(),
                casProperties.getServer().getLoginUrl(),
                casProperties.getServer().getLogoutUrl(),
                casProperties.getAuthn().getSamlIdp().getLogout().isForceSignedLogoutRequests(),
                casProperties.getAuthn().getSamlIdp().getLogout().isSingleLogoutCallbacksDisabled());
        return c;
    }

    @Bean
    @RefreshScope
    public IdPInitiatedProfileHandlerController idPInitiatedSamlProfileHandlerController() {
        final IdPInitiatedProfileHandlerController c = new IdPInitiatedProfileHandlerController(
                samlObjectSigner(),
                parserPool,
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver(),
                openSamlConfigBean,
                samlProfileSamlResponseBuilder(),
                authenticationContextClassMappings,
                casProperties.getServer().getPrefix(),
                casProperties.getServer().getName(),
                casProperties.getAuthn().getMfa().getRequestParameter(),
                casProperties.getServer().getLoginUrl(),
                casProperties.getServer().getLogoutUrl(),
                casProperties.getAuthn().getSamlIdp().getLogout().isForceSignedLogoutRequests(),
                casProperties.getAuthn().getSamlIdp().getLogout().isSingleLogoutCallbacksDisabled());
        return c;
    }

    @Bean
    @RefreshScope
    public SSOPostProfileCallbackHandlerController ssoPostProfileCallbackHandlerController() {
        final SSOPostProfileCallbackHandlerController c = new SSOPostProfileCallbackHandlerController(
                samlObjectSigner(),
                parserPool,
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver(),
                openSamlConfigBean,
                samlProfileSamlResponseBuilder(),
                authenticationContextClassMappings,
                casProperties.getServer().getPrefix(),
                casProperties.getServer().getName(),
                casProperties.getAuthn().getMfa().getRequestParameter(),
                casProperties.getServer().getLoginUrl(),
                casProperties.getServer().getLogoutUrl(),
                casProperties.getAuthn().getSamlIdp().getLogout().isForceSignedLogoutRequests(),
                casProperties.getAuthn().getSamlIdp().getLogout().isSingleLogoutCallbacksDisabled());
        return c;
    }

    @Bean
    @RefreshScope
    public ECPProfileHandlerController ecpProfileHandlerController() {
        return new ECPProfileHandlerController(samlObjectSigner(),
                parserPool,
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                defaultSamlRegisteredServiceCachingMetadataResolver(),
                openSamlConfigBean,
                samlProfileSamlResponseBuilder(),
                authenticationContextClassMappings,
                casProperties.getServer().getPrefix(),
                casProperties.getServer().getName(),
                casProperties.getAuthn().getMfa().getRequestParameter(),
                casProperties.getServer().getLoginUrl(),
                casProperties.getServer().getLogoutUrl(),
                casProperties.getAuthn().getSamlIdp().getLogout().isForceSignedLogoutRequests(),
                casProperties.getAuthn().getSamlIdp().getLogout().isSingleLogoutCallbacksDisabled());
    }

    @Bean
    @RefreshScope
    public SamlMetadataController samlMetadataController() {
        return new SamlMetadataController(shibbolethIdpMetadataAndCertificatesGenerationService());
    }
}
