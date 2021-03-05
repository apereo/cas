package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlanConfigurer;
import org.apereo.cas.logout.LogoutRedirectionStrategy;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;
import org.apereo.cas.pac4j.DistributedJEESessionStore;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.services.SamlIdPServiceRegistry;
import org.apereo.cas.support.saml.services.SamlIdPServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.HttpServletRequestXMLMessageDecodersMap;
import org.apereo.cas.support.saml.web.idp.profile.SamlIdPInitiatedProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.support.saml.web.idp.profile.artifact.SamlIdPSaml1ArtifactResolutionProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.validate.SamlIdPObjectSignatureValidator;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.validate.SamlObjectSignatureValidator;
import org.apereo.cas.support.saml.web.idp.profile.ecp.ECPSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.query.SamlIdPSaml2AttributeQueryProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.slo.SLOSamlIdPPostProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.slo.SLOSamlIdPRedirectProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPLogoutResponseObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPProfileSingleLogoutMessageCreator;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPSingleLogoutRedirectionStrategy;
import org.apereo.cas.support.saml.web.idp.profile.slo.SamlIdPSingleLogoutServiceMessageHandler;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOSamlIdPPostProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOSamlIdPPostSimpleSignProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.sso.SSOSamlIdPProfileCallbackHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.sso.UrlDecodingHTTPRedirectDeflateDecoder;
import org.apereo.cas.support.saml.web.idp.profile.sso.request.DefaultSSOSamlHttpRequestExtractor;
import org.apereo.cas.support.saml.web.idp.profile.sso.request.SSOSamlHttpRequestExtractor;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketFactory;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.InternalTicketValidator;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.ProtocolEndpointConfigurer;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.jasig.cas.client.validation.TicketValidator;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostSimpleSignDecoder;
import org.opensaml.saml.saml2.core.Response;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;

import java.util.List;

/**
 * This is {@link SamlIdPEndpointsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("samlIdPEndpointsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SamlIdPEndpointsConfiguration {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("singleLogoutServiceLogoutUrlBuilder")
    private ObjectProvider<SingleLogoutServiceLogoutUrlBuilder> singleLogoutServiceLogoutUrlBuilder;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private ObjectProvider<HttpClient> httpClient;

    @Autowired
    @Qualifier("shibboleth.VelocityEngine")
    private ObjectProvider<VelocityEngine> velocityEngineFactory;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private ObjectProvider<TicketFactory> ticketFactory;

    @Autowired
    @Qualifier("samlProfileSamlResponseBuilder")
    private ObjectProvider<SamlProfileObjectBuilder<Response>> samlProfileSamlResponseBuilder;

    @Autowired
    @Qualifier(SamlRegisteredServiceCachingMetadataResolver.DEFAULT_BEAN_NAME)
    private ObjectProvider<SamlRegisteredServiceCachingMetadataResolver> defaultSamlRegisteredServiceCachingMetadataResolver;
    
    @Autowired
    @Qualifier("samlIdPServiceFactory")
    private ObjectProvider<ServiceFactory> samlIdPServiceFactory;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("samlObjectSigner")
    private ObjectProvider<SamlIdPObjectSigner> samlObjectSigner;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CasCookieBuilder> ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("casSamlIdPMetadataResolver")
    private ObjectProvider<MetadataResolver> casSamlIdPMetadataResolver;

    @Autowired
    @Qualifier("samlProfileSamlSoap11ResponseBuilder")
    private ObjectProvider<SamlProfileObjectBuilder<org.opensaml.saml.saml2.ecp.Response>> samlProfileSamlSoap11ResponseBuilder;

    @Autowired
    @Qualifier("samlProfileSamlSoap11FaultResponseBuilder")
    private ObjectProvider<SamlProfileObjectBuilder<org.opensaml.saml.saml2.ecp.Response>> samlProfileSamlSoap11FaultResponseBuilder;

    @Autowired
    @Qualifier("samlProfileSamlArtifactResponseBuilder")
    private ObjectProvider<SamlProfileObjectBuilder<Response>> samlProfileSamlArtifactResponseBuilder;

    @Autowired
    @Qualifier("samlProfileSamlArtifactFaultResponseBuilder")
    private ObjectProvider<SamlProfileObjectBuilder<Response>> samlProfileSamlArtifactFaultResponseBuilder;

    @Autowired
    @Qualifier("samlProfileSamlAttributeQueryResponseBuilder")
    private ObjectProvider<SamlProfileObjectBuilder<Response>> samlProfileSamlAttributeQueryResponseBuilder;

    @Autowired
    @Qualifier("samlProfileSamlAttributeQueryFaultResponseBuilder")
    private ObjectProvider<SamlProfileObjectBuilder<Response>> samlProfileSamlAttributeQueryFaultResponseBuilder;

    @Autowired
    @Qualifier("samlAttributeQueryTicketFactory")
    private ObjectProvider<SamlAttributeQueryTicketFactory> samlAttributeQueryTicketFactory;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;
    
    @Autowired
    @Qualifier("samlArtifactTicketFactory")
    private ObjectProvider<SamlArtifactTicketFactory> samlArtifactTicketFactory;

    @ConditionalOnMissingBean(name = "samlIdPObjectSignatureValidator")
    @Bean
    public SamlObjectSignatureValidator samlIdPObjectSignatureValidator() {
        val algs = casProperties.getAuthn().getSamlIdp().getAlgs();
        return new SamlIdPObjectSignatureValidator(
            algs.getOverrideSignatureReferenceDigestMethods(),
            algs.getOverrideSignatureAlgorithms(),
            algs.getOverrideBlockedSignatureSigningAlgorithms(),
            algs.getOverrideAllowedSignatureSigningAlgorithms(),
            casSamlIdPMetadataResolver.getObject(),
            casProperties
        );
    }

    @ConditionalOnMissingBean(name = "samlObjectSignatureValidator")
    @Bean
    public SamlObjectSignatureValidator samlObjectSignatureValidator() {
        val algs = casProperties.getAuthn().getSamlIdp().getAlgs();
        return new SamlObjectSignatureValidator(
            algs.getOverrideSignatureReferenceDigestMethods(),
            algs.getOverrideSignatureAlgorithms(),
            algs.getOverrideBlockedSignatureSigningAlgorithms(),
            algs.getOverrideAllowedSignatureSigningAlgorithms(),
            casProperties
        );
    }

    @ConditionalOnMissingBean(name = "ssoSamlHttpRequestExtractor")
    @Bean
    public SSOSamlHttpRequestExtractor ssoSamlHttpRequestExtractor() {
        return new DefaultSSOSamlHttpRequestExtractor(openSamlConfigBean.getObject().getParserPool());
    }

    @Bean
    @ConditionalOnMissingBean(name = "ssoPostProfileHandlerDecoders")
    public HttpServletRequestXMLMessageDecodersMap ssoPostProfileHandlerDecoders() {
        val props = casProperties.getAuthn().getSamlIdp().getProfile().getSso();
        val decoders = new HttpServletRequestXMLMessageDecodersMap(HttpMethod.class);
        decoders.put(HttpMethod.GET, new UrlDecodingHTTPRedirectDeflateDecoder(props.isUrlDecodeRedirectRequest()));
        decoders.put(HttpMethod.POST, new HTTPPostDecoder());
        return decoders;
    }

    @Bean
    @RefreshScope
    public SSOSamlIdPPostProfileHandlerController ssoPostProfileHandlerController() {
        val context = getSamlProfileHandlerConfigurationContextBuilder()
            .responseBuilder(samlProfileSamlResponseBuilder.getObject())
            .samlMessageDecoders(ssoPostProfileHandlerDecoders())
            .build();
        return new SSOSamlIdPPostProfileHandlerController(context);
    }

    @Bean
    @ConditionalOnMissingBean(name = "ssoPostSimpleSignProfileHandlerDecoders")
    public HttpServletRequestXMLMessageDecodersMap ssoPostSimpleSignProfileHandlerDecoders() {
        val props = casProperties.getAuthn().getSamlIdp().getProfile().getSsoPostSimpleSign();
        val decoders = new HttpServletRequestXMLMessageDecodersMap(HttpMethod.class);
        decoders.put(HttpMethod.GET, new UrlDecodingHTTPRedirectDeflateDecoder(props.isUrlDecodeRedirectRequest()));
        decoders.put(HttpMethod.POST, new HTTPPostSimpleSignDecoder());
        return decoders;
    }

    @Bean
    @RefreshScope
    public SSOSamlIdPPostSimpleSignProfileHandlerController ssoPostSimpleSignProfileHandlerController() {
        val context = getSamlProfileHandlerConfigurationContextBuilder()
            .responseBuilder(samlProfileSamlResponseBuilder.getObject())
            .samlMessageDecoders(ssoPostSimpleSignProfileHandlerDecoders())
            .build();

        return new SSOSamlIdPPostSimpleSignProfileHandlerController(context);
    }

    @Bean
    @ConditionalOnMissingBean(name = "sloRedirectProfileHandlerDecoders")
    public HttpServletRequestXMLMessageDecodersMap sloRedirectProfileHandlerDecoders() {
        val props = casProperties.getAuthn().getSamlIdp().getProfile().getSlo();
        val decoders = new HttpServletRequestXMLMessageDecodersMap(HttpMethod.class);
        decoders.put(HttpMethod.GET, new UrlDecodingHTTPRedirectDeflateDecoder(props.isUrlDecodeRedirectRequest()));
        return decoders;
    }

    @Bean
    @RefreshScope
    public SLOSamlIdPRedirectProfileHandlerController sloRedirectProfileHandlerController() {
        val context = getSamlProfileHandlerConfigurationContextBuilder()
            .samlMessageDecoders(sloRedirectProfileHandlerDecoders())
            .build();
        return new SLOSamlIdPRedirectProfileHandlerController(context);
    }

    @Bean
    @ConditionalOnMissingBean(name = "sloPostProfileHandlerDecoders")
    public HttpServletRequestXMLMessageDecodersMap sloPostProfileHandlerDecoders() {
        val decoders = new HttpServletRequestXMLMessageDecodersMap(HttpMethod.class);
        decoders.put(HttpMethod.POST, new HTTPPostDecoder());
        return decoders;
    }

    @Bean
    @RefreshScope
    public SLOSamlIdPPostProfileHandlerController sloPostProfileHandlerController() {
        val context = getSamlProfileHandlerConfigurationContextBuilder()
            .samlMessageDecoders(sloPostProfileHandlerDecoders())
            .build();
        return new SLOSamlIdPPostProfileHandlerController(context);
    }

    @Bean
    @RefreshScope
    public SamlIdPInitiatedProfileHandlerController idpInitiatedSamlProfileHandlerController() {
        val context = getSamlProfileHandlerConfigurationContextBuilder()
            .samlObjectSignatureValidator(samlIdPObjectSignatureValidator())
            .build();
        return new SamlIdPInitiatedProfileHandlerController(context);
    }

    @Bean
    @RefreshScope
    public SSOSamlIdPProfileCallbackHandlerController ssoPostProfileCallbackHandlerController() {
        val context = getSamlProfileHandlerConfigurationContextBuilder().build();
        return new SSOSamlIdPProfileCallbackHandlerController(context);
    }

    @Bean
    @RefreshScope
    public ECPSamlIdPProfileHandlerController ecpProfileHandlerController() {
        val context = getSamlProfileHandlerConfigurationContextBuilder()
            .responseBuilder(samlProfileSamlSoap11ResponseBuilder.getObject())
            .samlFaultResponseBuilder(samlProfileSamlSoap11FaultResponseBuilder.getObject())
            .build();
        return new ECPSamlIdPProfileHandlerController(context);
    }

    @Bean
    @RefreshScope
    public SamlIdPSaml1ArtifactResolutionProfileHandlerController saml1ArtifactResolutionController() {
        val context = getSamlProfileHandlerConfigurationContextBuilder()
            .responseBuilder(samlProfileSamlArtifactResponseBuilder.getObject())
            .samlFaultResponseBuilder(samlProfileSamlArtifactFaultResponseBuilder.getObject())
            .build();
        return new SamlIdPSaml1ArtifactResolutionProfileHandlerController(context);
    }

    @ConditionalOnProperty(prefix = "cas.authn.saml-idp.core", name = "attribute-query-profile-enabled", havingValue = "true")
    @Bean
    @RefreshScope
    public SamlIdPSaml2AttributeQueryProfileHandlerController saml2AttributeQueryProfileHandlerController() {
        val context = getSamlProfileHandlerConfigurationContextBuilder()
            .responseBuilder(samlProfileSamlAttributeQueryResponseBuilder.getObject())
            .samlFaultResponseBuilder(samlProfileSamlAttributeQueryFaultResponseBuilder.getObject())
            .build();
        return new SamlIdPSaml2AttributeQueryProfileHandlerController(context);
    }

    @Bean
    public Service samlIdPCallbackService() {
        val service = casProperties.getServer().getPrefix().concat(SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK);
        return this.samlIdPServiceFactory.getObject().createService(service);
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlIdPServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer samlIdPServiceRegistryExecutionPlanConfigurer() {
        return plan -> {
            val callbackService = samlIdPCallbackService().getId().concat(".*");
            LOGGER.debug("Initializing SAML IdP callback service [{}]", callbackService);
            val service = new RegexRegisteredService();
            service.setId(RandomUtils.nextLong());
            service.setEvaluationOrder(Ordered.HIGHEST_PRECEDENCE);
            service.setName(service.getClass().getSimpleName());
            service.setDescription("SAML Authentication Request Callback");
            service.setServiceId(callbackService);
            plan.registerServiceRegistry(new SamlIdPServiceRegistry(applicationContext, service));
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlIdPServicesManagerRegisteredServiceLocator")
    public ServicesManagerRegisteredServiceLocator samlIdPServicesManagerRegisteredServiceLocator() {
        return new SamlIdPServicesManagerRegisteredServiceLocator(defaultSamlRegisteredServiceCachingMetadataResolver.getObject());
    }

    @ConditionalOnMissingBean(name = "samlIdPDistributedSessionCookieGenerator")
    @Bean
    @RefreshScope
    public CasCookieBuilder samlIdPDistributedSessionCookieGenerator() {
        val cookie = casProperties.getSessionReplication().getCookie();
        return CookieUtils.buildCookieRetrievingGenerator(cookie);
    }

    @ConditionalOnMissingBean(name = "samlIdPDistributedSessionStore")
    @Bean
    @RefreshScope
    public SessionStore samlIdPDistributedSessionStore() {
        val replicate = casProperties.getAuthn().getSamlIdp().getCore().isReplicateSessions();
        if (replicate) {
            return new DistributedJEESessionStore(centralAuthenticationService.getObject(),
                ticketFactory.getObject(), samlIdPDistributedSessionCookieGenerator());
        }
        return JEESessionStore.INSTANCE;
    }

    @ConditionalOnMissingBean(name = "samlIdPSingleLogoutRedirectionStrategy")
    @Bean
    @RefreshScope
    public LogoutRedirectionStrategy samlIdPSingleLogoutRedirectionStrategy() {
        return new SamlIdPSingleLogoutRedirectionStrategy(getSamlProfileHandlerConfigurationContextBuilder().build());
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlIdPTicketValidator")
    public TicketValidator samlIdPTicketValidator() {
        return new InternalTicketValidator(centralAuthenticationService.getObject(), samlIdPServiceFactory.getObject());
    }

    @ConditionalOnMissingBean(name = "samlLogoutBuilder")
    @Bean
    @RefreshScope
    public SingleLogoutMessageCreator samlLogoutBuilder() {
        return new SamlIdPProfileSingleLogoutMessageCreator(
            openSamlConfigBean.getObject(),
            servicesManager.getObject(),
            defaultSamlRegisteredServiceCachingMetadataResolver.getObject(),
            casProperties.getAuthn().getSamlIdp(),
            samlObjectSigner.getObject());
    }

    @ConditionalOnMissingBean(name = "samlSingleLogoutServiceMessageHandler")
    @Bean
    public SingleLogoutServiceMessageHandler samlSingleLogoutServiceMessageHandler() {
        return new SamlIdPSingleLogoutServiceMessageHandler(httpClient.getObject(),
            samlLogoutBuilder(),
            servicesManager.getObject(),
            singleLogoutServiceLogoutUrlBuilder.getObject(),
            casProperties.getSlo().isAsynchronous(),
            authenticationServiceSelectionPlan.getObject(),
            defaultSamlRegisteredServiceCachingMetadataResolver.getObject(),
            velocityEngineFactory.getObject(),
            openSamlConfigBean.getObject());
    }
    
    @ConditionalOnMissingBean(name = "casSamlIdPLogoutExecutionPlanConfigurer")
    @Bean
    @RefreshScope
    public LogoutExecutionPlanConfigurer casSamlIdPLogoutExecutionPlanConfigurer() {
        return plan -> {
            plan.registerLogoutRedirectionStrategy(samlIdPSingleLogoutRedirectionStrategy());
            plan.registerSingleLogoutServiceMessageHandler(samlSingleLogoutServiceMessageHandler());
        };
    }
    
    @Bean
    public SamlIdPLogoutResponseObjectBuilder samlIdPLogoutResponseObjectBuilder() {
        return new SamlIdPLogoutResponseObjectBuilder(openSamlConfigBean.getObject());
    }

    private SamlProfileHandlerConfigurationContext.SamlProfileHandlerConfigurationContextBuilder getSamlProfileHandlerConfigurationContextBuilder() {
        return SamlProfileHandlerConfigurationContext.builder()
            .samlObjectSigner(samlObjectSigner.getObject())
            .authenticationSystemSupport(authenticationSystemSupport.getObject())
            .servicesManager(servicesManager.getObject())
            .webApplicationServiceFactory(samlIdPServiceFactory.getObject())
            .samlRegisteredServiceCachingMetadataResolver(defaultSamlRegisteredServiceCachingMetadataResolver.getObject())
            .openSamlConfigBean(openSamlConfigBean.getObject())
            .casProperties(casProperties)
            .logoutResponseBuilder(samlIdPLogoutResponseObjectBuilder())
            .singleLogoutServiceLogoutUrlBuilder(singleLogoutServiceLogoutUrlBuilder.getObject())
            .artifactTicketFactory(samlArtifactTicketFactory.getObject())
            .samlObjectSignatureValidator(samlObjectSignatureValidator())
            .samlHttpRequestExtractor(ssoSamlHttpRequestExtractor())
            .responseBuilder(samlProfileSamlResponseBuilder.getObject())
            .ticketValidator(samlIdPTicketValidator())
            .ticketRegistry(ticketRegistry.getObject())
            .sessionStore(samlIdPDistributedSessionStore())
            .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator.getObject())
            .samlAttributeQueryTicketFactory(samlAttributeQueryTicketFactory.getObject())
            .samlDistributedSessionCookieGenerator(samlIdPDistributedSessionCookieGenerator())
            .callbackService(samlIdPCallbackService());
    }

    @Bean
    public ProtocolEndpointConfigurer samlIdPProtocolEndpointConfigurer() {
        return () -> List.of(StringUtils.prependIfMissing(SamlIdPConstants.BASE_ENDPOINT_SAML1, "/"),
            StringUtils.prependIfMissing(SamlIdPConstants.BASE_ENDPOINT_SAML2, "/"));
    }
}
