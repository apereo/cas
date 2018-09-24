package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPAlgorithmsProperties;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.services.SamlIdPServiceRegistry;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.IdPInitiatedProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.artifact.Saml1ArtifactResolutionProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSignatureValidator;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidator;
import org.apereo.cas.support.saml.web.idp.profile.ecp.ECPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.query.Saml2AttributeQueryProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.slo.SLOSamlPostProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.slo.SLOSamlRedirectProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOSamlPostProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOSamlPostSimpleSignProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOSamlProfileCallbackHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.sso.request.DefaultSSOSamlHttpRequestExtractor;
import org.apereo.cas.support.saml.web.idp.profile.sso.request.SSOSamlHttpRequestExtractor;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlIdPEndpointsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("samlIdPEndpointsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SamlIdPEndpointsConfiguration implements ServiceRegistryExecutionPlanConfigurer {
    @Autowired
    @Qualifier("casClientTicketValidator")
    private AbstractUrlBasedTicketValidator casClientTicketValidator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    @Qualifier("samlProfileSamlResponseBuilder")
    private SamlProfileObjectBuilder<Response> samlProfileSamlResponseBuilder;

    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    private SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("samlObjectSigner")
    private SamlIdPObjectSigner samlObjectSigner;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CookieRetrievingCookieGenerator> ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("casSamlIdPMetadataResolver")
    private MetadataResolver casSamlIdPMetadataResolver;

    @Autowired
    @Qualifier("samlProfileSamlSoap11ResponseBuilder")
    private SamlProfileObjectBuilder<org.opensaml.saml.saml2.ecp.Response> samlProfileSamlSoap11ResponseBuilder;

    @Autowired
    @Qualifier("samlProfileSamlSoap11FaultResponseBuilder")
    private SamlProfileObjectBuilder<org.opensaml.saml.saml2.ecp.Response> samlProfileSamlSoap11FaultResponseBuilder;

    @Autowired
    @Qualifier("samlProfileSamlArtifactResponseBuilder")
    private SamlProfileObjectBuilder<Response> samlProfileSamlArtifactResponseBuilder;

    @Autowired
    @Qualifier("samlProfileSamlArtifactFaultResponseBuilder")
    private SamlProfileObjectBuilder<Response> samlProfileSamlArtifactFaultResponseBuilder;

    @Autowired
    @Qualifier("samlProfileSamlAttributeQueryResponseBuilder")
    private SamlProfileObjectBuilder<Response> samlProfileSamlAttributeQueryResponseBuilder;

    @Autowired
    @Qualifier("samlProfileSamlAttributeQueryFaultResponseBuilder")
    private SamlProfileObjectBuilder<Response> samlProfileSamlAttributeQueryFaultResponseBuilder;

    @Autowired
    @Qualifier("samlAttributeQueryTicketFactory")
    private SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("samlArtifactTicketFactory")
    private ObjectProvider<SamlArtifactTicketFactory> samlArtifactTicketFactory;

    @ConditionalOnMissingBean(name = "samlIdPObjectSignatureValidator")
    @Bean
    public SamlObjectSignatureValidator samlIdPObjectSignatureValidator() {
        final SamlIdPAlgorithmsProperties algs = casProperties.getAuthn().getSamlIdp().getAlgs();
        return new SamlIdPObjectSignatureValidator(
            algs.getOverrideSignatureReferenceDigestMethods(),
            algs.getOverrideSignatureAlgorithms(),
            algs.getOverrideBlackListedSignatureSigningAlgorithms(),
            algs.getOverrideWhiteListedSignatureSigningAlgorithms(),
            casSamlIdPMetadataResolver,
            casProperties
        );
    }

    @ConditionalOnMissingBean(name = "samlObjectSignatureValidator")
    @Bean
    public SamlObjectSignatureValidator samlObjectSignatureValidator() {
        final SamlIdPAlgorithmsProperties algs = casProperties.getAuthn().getSamlIdp().getAlgs();
        return new SamlObjectSignatureValidator(
            algs.getOverrideSignatureReferenceDigestMethods(),
            algs.getOverrideSignatureAlgorithms(),
            algs.getOverrideBlackListedSignatureSigningAlgorithms(),
            algs.getOverrideWhiteListedSignatureSigningAlgorithms(),
            casProperties
        );
    }

    @ConditionalOnMissingBean(name = "ssoSamlHttpRequestExtractor")
    @Bean
    public SSOSamlHttpRequestExtractor ssoSamlHttpRequestExtractor() {
        return new DefaultSSOSamlHttpRequestExtractor(openSamlConfigBean.getParserPool());
    }

    @Bean
    @RefreshScope
    public SSOSamlPostProfileHandlerController ssoPostProfileHandlerController() {
        return new SSOSamlPostProfileHandlerController(
            samlObjectSigner,
            openSamlConfigBean.getParserPool(),
            authenticationSystemSupport,
            servicesManager,
            webApplicationServiceFactory,
            defaultSamlRegisteredServiceCachingMetadataResolver,
            openSamlConfigBean,
            samlProfileSamlResponseBuilder,
            casProperties,
            samlObjectSignatureValidator(),
            ssoSamlHttpRequestExtractor(),
            samlIdPCallbackService());
    }

    @Bean
    @RefreshScope
    public SSOSamlPostSimpleSignProfileHandlerController ssoPostSimpleSignProfileHandlerController() {
        return new SSOSamlPostSimpleSignProfileHandlerController(
            samlObjectSigner,
            openSamlConfigBean.getParserPool(),
            authenticationSystemSupport,
            servicesManager,
            webApplicationServiceFactory,
            defaultSamlRegisteredServiceCachingMetadataResolver,
            openSamlConfigBean,
            samlProfileSamlResponseBuilder,
            casProperties,
            samlObjectSignatureValidator(),
            ssoSamlHttpRequestExtractor(),
            samlIdPCallbackService());
    }


    @Bean
    @RefreshScope
    public SLOSamlRedirectProfileHandlerController sloRedirectProfileHandlerController() {
        return new SLOSamlRedirectProfileHandlerController(
            samlObjectSigner,
            openSamlConfigBean.getParserPool(),
            authenticationSystemSupport,
            servicesManager,
            webApplicationServiceFactory,
            defaultSamlRegisteredServiceCachingMetadataResolver,
            openSamlConfigBean,
            samlProfileSamlResponseBuilder,
            casProperties,
            samlObjectSignatureValidator(),
            ssoSamlHttpRequestExtractor(),
            samlIdPCallbackService());
    }

    @Bean
    @RefreshScope
    public SLOSamlPostProfileHandlerController sloPostProfileHandlerController() {
        return new SLOSamlPostProfileHandlerController(
            samlObjectSigner,
            openSamlConfigBean.getParserPool(),
            authenticationSystemSupport,
            servicesManager,
            webApplicationServiceFactory,
            defaultSamlRegisteredServiceCachingMetadataResolver,
            openSamlConfigBean,
            samlProfileSamlResponseBuilder,
            casProperties,
            samlObjectSignatureValidator(),
            ssoSamlHttpRequestExtractor(),
            samlIdPCallbackService());
    }

    @Bean
    @RefreshScope
    public IdPInitiatedProfileHandlerController idPInitiatedSamlProfileHandlerController() {
        return new IdPInitiatedProfileHandlerController(
            samlObjectSigner,
            openSamlConfigBean.getParserPool(),
            authenticationSystemSupport,
            servicesManager,
            webApplicationServiceFactory,
            defaultSamlRegisteredServiceCachingMetadataResolver,
            openSamlConfigBean,
            samlProfileSamlResponseBuilder,
            casProperties,
            samlIdPObjectSignatureValidator(),
            samlIdPCallbackService());
    }

    @Bean
    @RefreshScope
    public SSOSamlProfileCallbackHandlerController ssoPostProfileCallbackHandlerController() {
        return new SSOSamlProfileCallbackHandlerController(
            samlObjectSigner,
            openSamlConfigBean.getParserPool(),
            authenticationSystemSupport,
            servicesManager,
            webApplicationServiceFactory,
            defaultSamlRegisteredServiceCachingMetadataResolver,
            openSamlConfigBean,
            samlProfileSamlResponseBuilder,
            casProperties,
            samlObjectSignatureValidator(),
            this.casClientTicketValidator,
            samlIdPCallbackService());
    }

    @Bean
    @RefreshScope
    public ECPProfileHandlerController ecpProfileHandlerController() {
        return new ECPProfileHandlerController(samlObjectSigner,
            openSamlConfigBean.getParserPool(),
            authenticationSystemSupport,
            servicesManager,
            webApplicationServiceFactory,
            defaultSamlRegisteredServiceCachingMetadataResolver,
            openSamlConfigBean,
            samlProfileSamlSoap11ResponseBuilder,
            samlProfileSamlSoap11FaultResponseBuilder,
            casProperties,
            samlObjectSignatureValidator(),
            samlIdPCallbackService());
    }

    @Bean
    @RefreshScope
    public Saml1ArtifactResolutionProfileHandlerController saml1ArtifactResolutionController() {
        return new Saml1ArtifactResolutionProfileHandlerController(
            samlObjectSigner,
            openSamlConfigBean.getParserPool(),
            authenticationSystemSupport,
            servicesManager,
            webApplicationServiceFactory,
            defaultSamlRegisteredServiceCachingMetadataResolver,
            openSamlConfigBean,
            samlProfileSamlArtifactResponseBuilder,
            casProperties,
            samlObjectSignatureValidator(),
            ticketRegistry,
            samlArtifactTicketFactory.getIfAvailable(),
            samlProfileSamlArtifactFaultResponseBuilder,
            samlIdPCallbackService());
    }

    @ConditionalOnProperty(prefix = "cas.authn.samlIdp", name = "attributeQueryProfileEnabled", havingValue = "true")
    @Bean
    @RefreshScope
    public Saml2AttributeQueryProfileHandlerController saml2AttributeQueryProfileHandlerController() {
        return new Saml2AttributeQueryProfileHandlerController(
            samlObjectSigner,
            openSamlConfigBean.getParserPool(),
            authenticationSystemSupport,
            servicesManager,
            webApplicationServiceFactory,
            defaultSamlRegisteredServiceCachingMetadataResolver,
            openSamlConfigBean,
            samlProfileSamlAttributeQueryResponseBuilder,
            casProperties,
            samlObjectSignatureValidator(),
            ticketRegistry,
            samlProfileSamlAttributeQueryFaultResponseBuilder,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            samlAttributeQueryTicketFactory,
            samlIdPCallbackService());
    }

    @Bean
    public Service samlIdPCallbackService() {
        final String service = casProperties.getServer().getPrefix().concat(SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK);
        return this.webApplicationServiceFactory.createService(service);
    }

    @Override
    public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
        final String callbackService = samlIdPCallbackService().getId().concat(".*");
        LOGGER.debug("Initializing callback service [{}]", callbackService);
        final RegexRegisteredService service = new RegexRegisteredService();
        service.setId(Math.abs(RandomUtils.getNativeInstance().nextLong()));
        service.setEvaluationOrder(0);
        service.setName(service.getClass().getSimpleName());
        service.setDescription("SAML Authentication Request");
        service.setServiceId(callbackService);
        plan.registerServiceRegistry(new SamlIdPServiceRegistry(service));
    }
}
