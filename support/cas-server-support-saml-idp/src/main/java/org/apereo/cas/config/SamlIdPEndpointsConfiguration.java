package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPCoreProperties;
import org.apereo.cas.logout.LogoutExecutionPlanConfigurer;
import org.apereo.cas.logout.LogoutRedirectionStrategy;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;
import org.apereo.cas.pac4j.BrowserWebStorageSessionStore;
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
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectEncrypter;
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
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.InternalTicketValidator;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;

import java.util.List;

/**
 * This is {@link SamlIdPEndpointsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@Configuration(value = "samlIdPEndpointsConfiguration", proxyBeanMethods = false)
public class SamlIdPEndpointsConfiguration {

    @Configuration(value = "SamlIdPEndpointCryptoConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPEndpointCryptoConfiguration {

        @ConditionalOnMissingBean(name = "samlIdPObjectSignatureValidator")
        @Bean
        @Autowired
        public SamlObjectSignatureValidator samlIdPObjectSignatureValidator(
            final CasConfigurationProperties casProperties,
            @Qualifier("casSamlIdPMetadataResolver")
            final MetadataResolver casSamlIdPMetadataResolver) {
            val algs = casProperties.getAuthn().getSamlIdp().getAlgs();
            return new SamlIdPObjectSignatureValidator(algs.getOverrideSignatureReferenceDigestMethods(),
                algs.getOverrideSignatureAlgorithms(), algs.getOverrideBlockedSignatureSigningAlgorithms(),
                algs.getOverrideAllowedSignatureSigningAlgorithms(), casSamlIdPMetadataResolver, casProperties);
        }

        @ConditionalOnMissingBean(name = "samlObjectSignatureValidator")
        @Bean
        @Autowired
        public SamlObjectSignatureValidator samlObjectSignatureValidator(final CasConfigurationProperties casProperties) {
            val algs = casProperties.getAuthn().getSamlIdp().getAlgs();
            return new SamlObjectSignatureValidator(algs.getOverrideSignatureReferenceDigestMethods(),
                algs.getOverrideSignatureAlgorithms(), algs.getOverrideBlockedSignatureSigningAlgorithms(),
                algs.getOverrideAllowedSignatureSigningAlgorithms(), casProperties);
        }
    }

    @Configuration(value = "SamlIdPEndpointControllersConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPEndpointControllersConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SSOSamlIdPPostProfileHandlerController ssoPostProfileHandlerController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
            return new SSOSamlIdPPostProfileHandlerController(samlProfileHandlerConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SSOSamlIdPPostSimpleSignProfileHandlerController ssoPostSimpleSignProfileHandlerController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext,
            @Qualifier("ssoPostSimpleSignProfileHandlerDecoders")
            final HttpServletRequestXMLMessageDecodersMap ssoPostSimpleSignProfileHandlerDecoders) {
            samlProfileHandlerConfigurationContext.setSamlMessageDecoders(ssoPostSimpleSignProfileHandlerDecoders);
            return new SSOSamlIdPPostSimpleSignProfileHandlerController(samlProfileHandlerConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SLOSamlIdPRedirectProfileHandlerController sloRedirectProfileHandlerController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext,
            @Qualifier("sloRedirectProfileHandlerDecoders")
            final HttpServletRequestXMLMessageDecodersMap sloRedirectProfileHandlerDecoders) {
            samlProfileHandlerConfigurationContext.setSamlMessageDecoders(sloRedirectProfileHandlerDecoders);
            return new SLOSamlIdPRedirectProfileHandlerController(samlProfileHandlerConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SLOSamlIdPPostProfileHandlerController sloPostProfileHandlerController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext,
            @Qualifier("sloPostProfileHandlerDecoders")
            final HttpServletRequestXMLMessageDecodersMap sloPostProfileHandlerDecoders) {
            samlProfileHandlerConfigurationContext.setSamlMessageDecoders(sloPostProfileHandlerDecoders);
            return new SLOSamlIdPPostProfileHandlerController(samlProfileHandlerConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SamlIdPInitiatedProfileHandlerController idpInitiatedSamlProfileHandlerController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
            return new SamlIdPInitiatedProfileHandlerController(samlProfileHandlerConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SSOSamlIdPProfileCallbackHandlerController ssoPostProfileCallbackHandlerController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
            return new SSOSamlIdPProfileCallbackHandlerController(samlProfileHandlerConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public ECPSamlIdPProfileHandlerController ecpProfileHandlerController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext,
            @Qualifier("samlProfileSamlSoap11FaultResponseBuilder")
            final SamlProfileObjectBuilder<org.opensaml.saml.saml2.ecp.Response> samlProfileSamlSoap11FaultResponseBuilder,
            @Qualifier("samlProfileSamlSoap11ResponseBuilder")
            final SamlProfileObjectBuilder<org.opensaml.saml.saml2.ecp.Response> samlProfileSamlSoap11ResponseBuilder) {
            samlProfileHandlerConfigurationContext.setResponseBuilder(samlProfileSamlSoap11ResponseBuilder);
            samlProfileHandlerConfigurationContext.setSamlFaultResponseBuilder(samlProfileSamlSoap11FaultResponseBuilder);
            return new ECPSamlIdPProfileHandlerController(samlProfileHandlerConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SamlIdPSaml1ArtifactResolutionProfileHandlerController saml1ArtifactResolutionController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext,
            @Qualifier("samlProfileSamlArtifactFaultResponseBuilder")
            final SamlProfileObjectBuilder<Response> samlProfileSamlArtifactFaultResponseBuilder,
            @Qualifier("samlProfileSamlArtifactResponseBuilder")
            final SamlProfileObjectBuilder<Response> samlProfileSamlArtifactResponseBuilder) {
            samlProfileHandlerConfigurationContext.setSamlFaultResponseBuilder(samlProfileSamlArtifactFaultResponseBuilder);
            samlProfileHandlerConfigurationContext.setResponseBuilder(samlProfileSamlArtifactResponseBuilder);
            return new SamlIdPSaml1ArtifactResolutionProfileHandlerController(samlProfileHandlerConfigurationContext);
        }

        @ConditionalOnProperty(prefix = "cas.authn.saml-idp.core", name = "attribute-query-profile-enabled", havingValue = "true")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SamlIdPSaml2AttributeQueryProfileHandlerController saml2AttributeQueryProfileHandlerController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext,
            @Qualifier("samlProfileSamlAttributeQueryResponseBuilder")
            final SamlProfileObjectBuilder<Response> samlProfileSamlAttributeQueryResponseBuilder) {
            samlProfileHandlerConfigurationContext.setResponseBuilder(samlProfileSamlAttributeQueryResponseBuilder);
            return new SamlIdPSaml2AttributeQueryProfileHandlerController(samlProfileHandlerConfigurationContext);
        }
    }

    @Configuration(value = "SamlIdPEndpointDecoderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPEndpointDecoderConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "ssoPostProfileHandlerDecoders")
        @Autowired
        public HttpServletRequestXMLMessageDecodersMap ssoPostProfileHandlerDecoders(
            final CasConfigurationProperties casProperties) {
            val props = casProperties.getAuthn()
                .getSamlIdp()
                .getProfile()
                .getSso();
            val decoders = new HttpServletRequestXMLMessageDecodersMap(HttpMethod.class);
            decoders.put(HttpMethod.GET, new UrlDecodingHTTPRedirectDeflateDecoder(props.isUrlDecodeRedirectRequest()));
            decoders.put(HttpMethod.POST, new HTTPPostDecoder());
            return decoders;
        }

        @Bean
        @ConditionalOnMissingBean(name = "ssoPostSimpleSignProfileHandlerDecoders")
        @Autowired
        public HttpServletRequestXMLMessageDecodersMap ssoPostSimpleSignProfileHandlerDecoders(
            final CasConfigurationProperties casProperties) {
            val props = casProperties.getAuthn().getSamlIdp().getProfile().getSsoPostSimpleSign();
            val decoders = new HttpServletRequestXMLMessageDecodersMap(HttpMethod.class);
            decoders.put(HttpMethod.GET, new UrlDecodingHTTPRedirectDeflateDecoder(props.isUrlDecodeRedirectRequest()));
            decoders.put(HttpMethod.POST, new HTTPPostSimpleSignDecoder());
            return decoders;
        }

        @Bean
        @ConditionalOnMissingBean(name = "sloRedirectProfileHandlerDecoders")
        @Autowired
        public HttpServletRequestXMLMessageDecodersMap sloRedirectProfileHandlerDecoders(final CasConfigurationProperties casProperties) {
            val props = casProperties.getAuthn().getSamlIdp().getProfile().getSlo();
            val decoders = new HttpServletRequestXMLMessageDecodersMap(HttpMethod.class);
            decoders.put(HttpMethod.GET, new UrlDecodingHTTPRedirectDeflateDecoder(props.isUrlDecodeRedirectRequest()));
            return decoders;
        }


        @Bean
        @ConditionalOnMissingBean(name = "sloPostProfileHandlerDecoders")
        public HttpServletRequestXMLMessageDecodersMap sloPostProfileHandlerDecoders() {
            val decoders = new HttpServletRequestXMLMessageDecodersMap(HttpMethod.class);
            decoders.put(HttpMethod.POST, new HTTPPostDecoder());
            return decoders;
        }

    }

    @Configuration(value = "SamlIdPEndpointsLogoutResponseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPEndpointsLogoutResponseConfiguration {
        @ConditionalOnMissingBean(name = "samlIdPSingleLogoutRedirectionStrategy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public LogoutRedirectionStrategy samlIdPSingleLogoutRedirectionStrategy(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
            return new SamlIdPSingleLogoutRedirectionStrategy(samlProfileHandlerConfigurationContext);
        }
    }

    @Configuration(value = "SamlIdPEndpointLogoutConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPEndpointLogoutConfiguration {
        @ConditionalOnMissingBean(name = "samlLogoutBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SingleLogoutMessageCreator samlLogoutBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
            final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver,
            @Qualifier("samlObjectSigner")
            final SamlIdPObjectSigner samlObjectSigner) {
            return new SamlIdPProfileSingleLogoutMessageCreator(openSamlConfigBean, servicesManager,
                defaultSamlRegisteredServiceCachingMetadataResolver, casProperties.getAuthn()
                .getSamlIdp(), samlObjectSigner);
        }

        @ConditionalOnMissingBean(name = "samlSingleLogoutServiceMessageHandler")
        @Bean
        @Autowired
        public SingleLogoutServiceMessageHandler samlSingleLogoutServiceMessageHandler(
            final CasConfigurationProperties casProperties,
            @Qualifier("samlLogoutBuilder")
            final SingleLogoutMessageCreator samlLogoutBuilder,
            @Qualifier("singleLogoutServiceLogoutUrlBuilder")
            final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder,
            @Qualifier("httpClient")
            final HttpClient httpClient,
            @Qualifier("velocityEngineFactory")
            final VelocityEngine velocityEngineFactory,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
            final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver) {
            return new SamlIdPSingleLogoutServiceMessageHandler(httpClient, samlLogoutBuilder, servicesManager,
                singleLogoutServiceLogoutUrlBuilder, casProperties.getSlo().isAsynchronous(),
                authenticationServiceSelectionPlan, defaultSamlRegisteredServiceCachingMetadataResolver,
                velocityEngineFactory, openSamlConfigBean);
        }


        @Bean
        public SamlIdPLogoutResponseObjectBuilder samlIdPLogoutResponseObjectBuilder(
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new SamlIdPLogoutResponseObjectBuilder(openSamlConfigBean);
        }
    }

    @Configuration(value = "SamlIdPEndpointsLogoutExecutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPEndpointsLogoutExecutionConfiguration {
        @ConditionalOnMissingBean(name = "casSamlIdPLogoutExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LogoutExecutionPlanConfigurer casSamlIdPLogoutExecutionPlanConfigurer(
            @Qualifier("samlIdPSingleLogoutRedirectionStrategy")
            final LogoutRedirectionStrategy samlIdPSingleLogoutRedirectionStrategy,
            @Qualifier("samlSingleLogoutServiceMessageHandler")
            final SingleLogoutServiceMessageHandler samlSingleLogoutServiceMessageHandler) {
            return plan -> {
                plan.registerLogoutRedirectionStrategy(samlIdPSingleLogoutRedirectionStrategy);
                plan.registerSingleLogoutServiceMessageHandler(samlSingleLogoutServiceMessageHandler);
            };
        }
    }

    @Configuration(value = "SamlIdPServicesConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPServicesConfiguration {
        @Bean
        @Autowired
        public Service samlIdPCallbackService(
            @Qualifier("samlIdPServiceFactory")
            final ServiceFactory samlIdPServiceFactory,
            final CasConfigurationProperties casProperties) {
            val service = casProperties.getServer()
                .getPrefix()
                .concat(SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_CALLBACK);
            return samlIdPServiceFactory.createService(service);
        }

        @Bean
        @ConditionalOnMissingBean(name = "samlIdPServiceRegistryExecutionPlanConfigurer")
        @Autowired
        public ServiceRegistryExecutionPlanConfigurer samlIdPServiceRegistryExecutionPlanConfigurer(final ConfigurableApplicationContext applicationContext,
                                                                                                    @Qualifier("samlIdPCallbackService")
                                                                                                    final Service samlIdPCallbackService) {
            return plan -> {
                val callbackService = samlIdPCallbackService.getId()
                    .concat(".*");
                LOGGER.debug("Initializing SAML IdP callback service [{}]", callbackService);
                val service = new RegexRegisteredService();
                service.setId(RandomUtils.nextLong());
                service.setEvaluationOrder(Ordered.HIGHEST_PRECEDENCE);
                service.setName(service.getClass()
                    .getSimpleName());
                service.setDescription("SAML Authentication Request Callback");
                service.setServiceId(callbackService);
                plan.registerServiceRegistry(new SamlIdPServiceRegistry(applicationContext, service));
            };
        }

        @Bean
        @ConditionalOnMissingBean(name = "samlIdPServicesManagerRegisteredServiceLocator")
        public ServicesManagerRegisteredServiceLocator samlIdPServicesManagerRegisteredServiceLocator(
            @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
            final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver) {
            return new SamlIdPServicesManagerRegisteredServiceLocator(defaultSamlRegisteredServiceCachingMetadataResolver);
        }

    }

    @Configuration(value = "SamlIdPEndpointCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPEndpointCoreConfiguration {

        @ConditionalOnMissingBean(name = "ssoSamlHttpRequestExtractor")
        @Bean
        public SSOSamlHttpRequestExtractor ssoSamlHttpRequestExtractor(
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new DefaultSSOSamlHttpRequestExtractor(openSamlConfigBean.getParserPool());
        }

        @Bean
        @ConditionalOnMissingBean(name = "samlIdPTicketValidator")
        @Autowired
        public TicketValidator samlIdPTicketValidator(
            @Qualifier(ServicesManager.BEAN_NAME)

            final ServicesManager servicesManager,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("samlIdPServiceFactory")
            final ServiceFactory samlIdPServiceFactory,
            @Qualifier("authenticationAttributeReleasePolicy")
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy) {
            return new InternalTicketValidator(centralAuthenticationService, samlIdPServiceFactory, authenticationAttributeReleasePolicy, servicesManager);
        }

        @Bean
        public ProtocolEndpointWebSecurityConfigurer<Void> samlIdPProtocolEndpointConfigurer() {
            return new ProtocolEndpointWebSecurityConfigurer<>() {

                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of(StringUtils.prependIfMissing(SamlIdPConstants.BASE_ENDPOINT_SAML1, "/"),
                        StringUtils.prependIfMissing(SamlIdPConstants.BASE_ENDPOINT_SAML2, "/"));
                }
            };
        }

        @ConditionalOnMissingBean(name = "samlIdPDistributedSessionCookieGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public CasCookieBuilder samlIdPDistributedSessionCookieGenerator(final CasConfigurationProperties casProperties) {
            val cookie = casProperties.getSessionReplication().getCookie();
            return CookieUtils.buildCookieRetrievingGenerator(cookie);
        }

        @ConditionalOnMissingBean(name = DistributedJEESessionStore.DEFAULT_BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SessionStore samlIdPDistributedSessionStore(
            final CasConfigurationProperties casProperties,
            @Qualifier("samlIdPDistributedSessionCookieGenerator")
            final CasCookieBuilder samlIdPDistributedSessionCookieGenerator,
            @Qualifier("webflowCipherExecutor")
            final CipherExecutor webflowCipherExecutor,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("defaultTicketFactory")
            final TicketFactory ticketFactory) {
            val type = casProperties.getAuthn().getSamlIdp().getCore().getSessionStorageType();
            if (type == SamlIdPCoreProperties.SessionStorageTypes.TICKET_REGISTRY) {
                return new DistributedJEESessionStore(centralAuthenticationService, ticketFactory, samlIdPDistributedSessionCookieGenerator);
            }
            if (type == SamlIdPCoreProperties.SessionStorageTypes.BROWSER_SESSION_STORAGE) {
                return new BrowserWebStorageSessionStore(webflowCipherExecutor);
            }
            return JEESessionStore.INSTANCE;
        }
    }

    @Configuration(value = "SamlIdPExecutionContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPExecutionContextConfiguration {
        @Bean
        @Autowired
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext(
            @Qualifier("authenticationAttributeReleasePolicy")
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
            @Qualifier("samlIdPCallbackService")
            final Service samlIdPCallbackService,
            @Qualifier("samlObjectEncrypter")
            final SamlIdPObjectEncrypter samlObjectEncrypter,
            @Qualifier("samlObjectSigner")
            final SamlIdPObjectSigner samlObjectSigner,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
            final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver,
            @Qualifier("samlIdPServiceFactory")
            final ServiceFactory samlIdPServiceFactory,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            final CasConfigurationProperties casProperties,
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("samlIdPTicketValidator")
            final TicketValidator samlIdPTicketValidator,
            @Qualifier("ssoSamlHttpRequestExtractor")
            final SSOSamlHttpRequestExtractor ssoSamlHttpRequestExtractor,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier("samlObjectSignatureValidator")
            final SamlObjectSignatureValidator samlObjectSignatureValidator,
            @Qualifier("singleSignOnParticipationStrategy")
            final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy,
            @Qualifier("singleLogoutServiceLogoutUrlBuilder")
            final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder,
            @Qualifier("samlIdPLogoutResponseObjectBuilder")
            final SamlIdPLogoutResponseObjectBuilder samlIdPLogoutResponseObjectBuilder,
            @Qualifier("samlIdPDistributedSessionCookieGenerator")
            final CasCookieBuilder samlIdPDistributedSessionCookieGenerator,
            @Qualifier("ticketGrantingTicketCookieGenerator")
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier("samlIdPDistributedSessionStore")
            final SessionStore samlIdPDistributedSessionStore,
            @Qualifier("samlProfileSamlResponseBuilder")
            final SamlProfileObjectBuilder<Response> samlProfileSamlResponseBuilder,
            @Qualifier("samlProfileSamlAttributeQueryFaultResponseBuilder")
            final SamlProfileObjectBuilder<Response> samlProfileSamlAttributeQueryFaultResponseBuilder,
            @Qualifier("defaultTicketFactory")
            final TicketFactory defaultTicketFactory,
            @Qualifier("ssoPostProfileHandlerDecoders")
            final HttpServletRequestXMLMessageDecodersMap ssoPostProfileHandlerDecoders) {
            return SamlProfileHandlerConfigurationContext.builder()
                .samlMessageDecoders(ssoPostProfileHandlerDecoders)
                .authenticationAttributeReleasePolicy(authenticationAttributeReleasePolicy)
                .samlObjectSigner(samlObjectSigner)
                .ticketFactory(defaultTicketFactory)
                .samlObjectEncrypter(samlObjectEncrypter)
                .authenticationSystemSupport(authenticationSystemSupport)
                .servicesManager(servicesManager)
                .webApplicationServiceFactory(samlIdPServiceFactory)
                .samlRegisteredServiceCachingMetadataResolver(defaultSamlRegisteredServiceCachingMetadataResolver)
                .openSamlConfigBean(openSamlConfigBean)
                .casProperties(casProperties)
                .ticketRegistrySupport(ticketRegistrySupport)
                .singleSignOnParticipationStrategy(singleSignOnParticipationStrategy)
                .logoutResponseBuilder(samlIdPLogoutResponseObjectBuilder)
                .singleLogoutServiceLogoutUrlBuilder(singleLogoutServiceLogoutUrlBuilder)
                .samlObjectSignatureValidator(samlObjectSignatureValidator)
                .samlHttpRequestExtractor(ssoSamlHttpRequestExtractor)
                .responseBuilder(samlProfileSamlResponseBuilder)
                .ticketValidator(samlIdPTicketValidator)
                .ticketRegistry(ticketRegistry)
                .sessionStore(samlIdPDistributedSessionStore)
                .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator)
                .samlDistributedSessionCookieGenerator(samlIdPDistributedSessionCookieGenerator)
                .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer)
                .callbackService(samlIdPCallbackService)
                .samlFaultResponseBuilder(samlProfileSamlAttributeQueryFaultResponseBuilder)
                .build();
        }
    }
}
