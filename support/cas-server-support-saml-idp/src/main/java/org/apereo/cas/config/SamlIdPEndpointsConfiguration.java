package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.interrupt.InterruptCookieProperties;
import org.apereo.cas.logout.LogoutExecutionPlanConfigurer;
import org.apereo.cas.logout.LogoutRedirectionStrategy;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.pac4j.BrowserWebStorageSessionStore;
import org.apereo.cas.pac4j.TicketRegistrySessionStore;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.idp.SamlIdPDistributedSessionCookieCipherExecutor;
import org.apereo.cas.support.saml.services.SamlIdPServiceRegistry;
import org.apereo.cas.support.saml.services.SamlIdPServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.util.Saml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.HttpServletRequestXMLMessageDecodersMap;
import org.apereo.cas.support.saml.web.idp.profile.SamlIdPInitiatedProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.support.saml.web.idp.profile.XMLMessageDecodersMap;
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
import org.apereo.cas.support.saml.web.idp.web.SamlIdPInfoEndpointContributor;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.InternalTicketValidator;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.cipher.DefaultCipherExecutorResolver;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.TicketValidator;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.mgmr.DefaultCasCookieValueManager;
import org.apereo.cas.web.support.mgmr.DefaultCookieSameSitePolicy;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostSimpleSignDecoder;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.soap.soap11.Envelope;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.actuate.autoconfigure.info.ConditionalOnEnabledInfoContributor;
import org.springframework.boot.actuate.autoconfigure.info.InfoContributorFallback;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLIdentityProvider)
@Configuration(value = "SamlIdPEndpointsConfiguration", proxyBeanMethods = false)
class SamlIdPEndpointsConfiguration {

    @Configuration(value = "SamlIdPEndpointCryptoConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPEndpointCryptoConfiguration {

        @ConditionalOnMissingBean(name = "samlIdPObjectSignatureValidator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
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
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlObjectSignatureValidator samlObjectSignatureValidator(final CasConfigurationProperties casProperties) {
            val algs = casProperties.getAuthn().getSamlIdp().getAlgs();
            return new SamlObjectSignatureValidator(algs.getOverrideSignatureReferenceDigestMethods(),
                algs.getOverrideSignatureAlgorithms(), algs.getOverrideBlockedSignatureSigningAlgorithms(),
                algs.getOverrideAllowedSignatureSigningAlgorithms(), casProperties);
        }
    }

    @Configuration(value = "SamlIdPEndpointControllersConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPEndpointControllersConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SSOSamlIdPPostProfileHandlerController ssoPostProfileHandlerController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
            return new SSOSamlIdPPostProfileHandlerController(samlProfileHandlerConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SSOSamlIdPPostSimpleSignProfileHandlerController ssoPostSimpleSignProfileHandlerController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext,
            @Qualifier("ssoPostSimpleSignProfileHandlerDecoders")
            final XMLMessageDecodersMap ssoPostSimpleSignProfileHandlerDecoders) {
            samlProfileHandlerConfigurationContext.setSamlMessageDecoders(ssoPostSimpleSignProfileHandlerDecoders);
            return new SSOSamlIdPPostSimpleSignProfileHandlerController(samlProfileHandlerConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SLOSamlIdPRedirectProfileHandlerController sloRedirectProfileHandlerController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext,
            @Qualifier("sloRedirectProfileHandlerDecoders")
            final XMLMessageDecodersMap sloRedirectProfileHandlerDecoders) {
            samlProfileHandlerConfigurationContext.setSamlMessageDecoders(sloRedirectProfileHandlerDecoders);
            return new SLOSamlIdPRedirectProfileHandlerController(samlProfileHandlerConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SLOSamlIdPPostProfileHandlerController sloPostProfileHandlerController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext,
            @Qualifier("sloPostProfileHandlerDecoders")
            final XMLMessageDecodersMap sloPostProfileHandlerDecoders) {
            samlProfileHandlerConfigurationContext.setSamlMessageDecoders(sloPostProfileHandlerDecoders);
            return new SLOSamlIdPPostProfileHandlerController(samlProfileHandlerConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlIdPInitiatedProfileHandlerController idpInitiatedSamlProfileHandlerController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext,
            @Qualifier("samlIdPObjectSignatureValidator")
            final SamlObjectSignatureValidator samlObjectSignatureValidator) {
            samlProfileHandlerConfigurationContext.setSamlObjectSignatureValidator(samlObjectSignatureValidator);
            return new SamlIdPInitiatedProfileHandlerController(samlProfileHandlerConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SSOSamlIdPProfileCallbackHandlerController ssoPostProfileCallbackHandlerController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
            return new SSOSamlIdPProfileCallbackHandlerController(samlProfileHandlerConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ECPSamlIdPProfileHandlerController ecpProfileHandlerController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext,
            @Qualifier("samlProfileSamlSoap11FaultResponseBuilder")
            final SamlProfileObjectBuilder<Envelope> samlProfileSamlSoap11FaultResponseBuilder,
            @Qualifier("samlProfileSamlSoap11ResponseBuilder")
            final SamlProfileObjectBuilder<Envelope> samlProfileSamlSoap11ResponseBuilder) {
            samlProfileHandlerConfigurationContext.setResponseBuilder(samlProfileSamlSoap11ResponseBuilder);
            samlProfileHandlerConfigurationContext.setSamlFaultResponseBuilder(samlProfileSamlSoap11FaultResponseBuilder);
            return new ECPSamlIdPProfileHandlerController(samlProfileHandlerConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlIdPSaml1ArtifactResolutionProfileHandlerController saml1ArtifactResolutionController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext,
            @Qualifier("samlProfileSamlArtifactFaultResponseBuilder")
            final SamlProfileObjectBuilder<Envelope> samlProfileSamlArtifactFaultResponseBuilder,
            @Qualifier("samlProfileSamlArtifactResponseBuilder")
            final SamlProfileObjectBuilder<Envelope> samlProfileSamlArtifactResponseBuilder) {
            samlProfileHandlerConfigurationContext.setSamlFaultResponseBuilder(samlProfileSamlArtifactFaultResponseBuilder);
            samlProfileHandlerConfigurationContext.setResponseBuilder(samlProfileSamlArtifactResponseBuilder);
            return new SamlIdPSaml1ArtifactResolutionProfileHandlerController(samlProfileHandlerConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlIdPSaml2AttributeQueryProfileHandlerController saml2AttributeQueryProfileHandlerController(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext,
            @Qualifier("samlProfileSamlAttributeQueryResponseBuilder")
            final SamlProfileObjectBuilder<Envelope> samlProfileSamlAttributeQueryResponseBuilder) {
            samlProfileHandlerConfigurationContext.setResponseBuilder(samlProfileSamlAttributeQueryResponseBuilder);
            return new SamlIdPSaml2AttributeQueryProfileHandlerController(samlProfileHandlerConfigurationContext);
        }
    }

    @Configuration(value = "SamlIdPEndpointDecoderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPEndpointDecoderConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "ssoPostProfileHandlerDecoders")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public XMLMessageDecodersMap ssoPostProfileHandlerDecoders(
            final CasConfigurationProperties casProperties) {
            val props = casProperties.getAuthn().getSamlIdp().getProfile().getSso();
            val decoders = new HttpServletRequestXMLMessageDecodersMap();
            decoders.put(HttpMethod.GET, new UrlDecodingHTTPRedirectDeflateDecoder(props.isUrlDecodeRedirectRequest()));
            decoders.put(HttpMethod.POST, new HTTPPostDecoder());
            return decoders;
        }

        @Bean
        @ConditionalOnMissingBean(name = "ssoPostSimpleSignProfileHandlerDecoders")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public XMLMessageDecodersMap ssoPostSimpleSignProfileHandlerDecoders(
            final CasConfigurationProperties casProperties) {
            val props = casProperties.getAuthn().getSamlIdp().getProfile().getSsoPostSimpleSign();
            val decoders = new HttpServletRequestXMLMessageDecodersMap();
            decoders.put(HttpMethod.GET, new UrlDecodingHTTPRedirectDeflateDecoder(props.isUrlDecodeRedirectRequest()));
            decoders.put(HttpMethod.POST, new HTTPPostSimpleSignDecoder());
            return decoders;
        }

        @Bean
        @ConditionalOnMissingBean(name = "sloRedirectProfileHandlerDecoders")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public XMLMessageDecodersMap sloRedirectProfileHandlerDecoders(final CasConfigurationProperties casProperties) {
            val props = casProperties.getAuthn().getSamlIdp().getProfile().getSlo();
            val decoders = new HttpServletRequestXMLMessageDecodersMap();
            decoders.put(HttpMethod.GET, new UrlDecodingHTTPRedirectDeflateDecoder(props.isUrlDecodeRedirectRequest()));
            return decoders;
        }


        @Bean
        @ConditionalOnMissingBean(name = "sloPostProfileHandlerDecoders")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public XMLMessageDecodersMap sloPostProfileHandlerDecoders() {
            val decoders = new HttpServletRequestXMLMessageDecodersMap();
            decoders.put(HttpMethod.POST, new HTTPPostDecoder());
            return decoders;
        }

    }

    @Configuration(value = "SamlIdPEndpointsLogoutResponseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPEndpointsLogoutResponseConfiguration {
        @ConditionalOnMissingBean(name = "samlIdPSingleLogoutRedirectionStrategy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LogoutRedirectionStrategy samlIdPSingleLogoutRedirectionStrategy(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
            return new SamlIdPSingleLogoutRedirectionStrategy(samlProfileHandlerConfigurationContext);
        }
    }

    @Configuration(value = "SamlIdPEndpointLogoutConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPEndpointLogoutConfiguration {
        @ConditionalOnMissingBean(name = "samlLogoutBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SingleLogoutMessageCreator samlLogoutBuilder(
            @Qualifier("samlProfileHandlerConfigurationContext")
            final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
            return new SamlIdPProfileSingleLogoutMessageCreator(samlProfileHandlerConfigurationContext);
        }

        @ConditionalOnMissingBean(name = "samlSingleLogoutServiceMessageHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SingleLogoutServiceMessageHandler samlSingleLogoutServiceMessageHandler(
            final CasConfigurationProperties casProperties,
            @Qualifier("samlLogoutBuilder")
            final SingleLogoutMessageCreator samlLogoutBuilder,
            @Qualifier("singleLogoutServiceLogoutUrlBuilder")
            final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder,
            @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
            final HttpClient httpClient,
            @Qualifier("velocityEngineFactory")
            final VelocityEngine velocityEngineFactory,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier(SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME)
            final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver) {
            return new SamlIdPSingleLogoutServiceMessageHandler(httpClient, samlLogoutBuilder, servicesManager,
                singleLogoutServiceLogoutUrlBuilder, casProperties.getSlo().isAsynchronous(),
                authenticationServiceSelectionPlan, defaultSamlRegisteredServiceCachingMetadataResolver,
                velocityEngineFactory, openSamlConfigBean);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Saml20ObjectBuilder samlIdPLogoutResponseObjectBuilder(
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new SamlIdPLogoutResponseObjectBuilder(openSamlConfigBean);
        }
    }

    @Configuration(value = "SamlIdPEndpointsLogoutExecutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPEndpointsLogoutExecutionConfiguration {
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
    static class SamlIdPServicesConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
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
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServiceRegistryExecutionPlanConfigurer samlIdPServiceRegistryExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("samlIdPCallbackService")
            final Service samlIdPCallbackService) {
            return plan -> {
                val callbackService = samlIdPCallbackService.getId().concat(".*");
                LOGGER.debug("Initializing SAML IdP callback service [{}]", callbackService);
                val service = new CasRegisteredService();
                service.setEvaluationOrder(Ordered.HIGHEST_PRECEDENCE);
                service.setName(service.getClass().getSimpleName());
                service.setDescription("SAML2 Authentication Request Callback");
                service.setServiceId(callbackService);
                service.markAsInternal();
                plan.registerServiceRegistry(new SamlIdPServiceRegistry(applicationContext, service));
            };
        }

        @Bean
        @ConditionalOnMissingBean(name = "samlIdPServicesManagerRegisteredServiceLocator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServicesManagerRegisteredServiceLocator samlIdPServicesManagerRegisteredServiceLocator(
            @Qualifier(SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME)
            final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver) {
            return new SamlIdPServicesManagerRegisteredServiceLocator(defaultSamlRegisteredServiceCachingMetadataResolver);
        }

    }

    @Configuration(value = "SamlIdPEndpointCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPEndpointCoreConfiguration {

        @ConditionalOnMissingBean(name = "ssoSamlHttpRequestExtractor")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SSOSamlHttpRequestExtractor ssoSamlHttpRequestExtractor(
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new DefaultSSOSamlHttpRequestExtractor(openSamlConfigBean.getParserPool());
        }

        @Bean
        @ConditionalOnMissingBean(name = "samlIdPTicketValidator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketValidator samlIdPTicketValidator(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("samlIdPServiceFactory")
            final ServiceFactory samlIdPServiceFactory,
            @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy) {
            return new InternalTicketValidator(centralAuthenticationService, samlIdPServiceFactory,
                authenticationAttributeReleasePolicy, servicesManager);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebSecurityConfigurer<Void> samlIdPProtocolEndpointConfigurer() {
            return new CasWebSecurityConfigurer<>() {

                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of(
                        Strings.CI.prependIfMissing(SamlIdPConstants.BASE_ENDPOINT_IDP, "/"),
                        Strings.CI.prependIfMissing(SamlIdPConstants.BASE_ENDPOINT_SAML1, "/"),
                        Strings.CI.prependIfMissing(SamlIdPConstants.BASE_ENDPOINT_SAML2, "/"));
                }
            };
        }

        @ConditionalOnMissingBean(name = "samlIdPDistributedSessionCookieCipherExecutor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public CipherExecutor samlIdPDistributedSessionCookieCipherExecutor(final CasConfigurationProperties casProperties) {
            val type = casProperties.getAuthn().getSamlIdp().getCore().getSessionStorageType();
            return FunctionUtils.doIf(type.isTicketRegistry(),
                () -> {
                    val cookie = casProperties.getAuthn().getSamlIdp().getCore().getSessionReplication().getCookie();
                    val crypto = cookie.getCrypto();
                    var enabled = crypto.isEnabled();
                    if (!enabled && StringUtils.isNotBlank(crypto.getEncryption().getKey())
                        && StringUtils.isNotBlank(crypto.getSigning().getKey())) {
                        LOGGER.warn("Encryption/Signing is not enabled explicitly in the configuration for cookie [{}], yet signing/encryption keys "
                                    + "are defined for operations. CAS will proceed to enable the cookie encryption/signing functionality.", cookie.getName());
                        enabled = true;
                    }
                    return enabled
                        ? CipherExecutorUtils.newStringCipherExecutor(crypto, SamlIdPDistributedSessionCookieCipherExecutor.class)
                        : CipherExecutor.noOp();
                },
                CipherExecutor::noOp).get();
        }

        @ConditionalOnMissingBean(name = "samlIdPDistributedSessionCookieGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasCookieBuilder samlIdPDistributedSessionCookieGenerator(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier(GeoLocationService.BEAN_NAME)
            final ObjectProvider<GeoLocationService> geoLocationService,
            @Qualifier("samlIdPDistributedSessionCookieCipherExecutor")
            final CipherExecutor samlIdPDistributedSessionCookieCipherExecutor,
            final CasConfigurationProperties casProperties) {

            val cipherExecutorResolver = new DefaultCipherExecutorResolver(samlIdPDistributedSessionCookieCipherExecutor, tenantExtractor,
                InterruptCookieProperties.class, bindingContext -> {
                val properties = bindingContext.value();
                val crypto = properties.getAuthn().getSamlIdp().getCore().getSessionReplication().getCookie().getCrypto();
                return CipherExecutorUtils.newStringCipherExecutor(crypto, SamlIdPDistributedSessionCookieCipherExecutor.class);
            });
            
            val cookie = casProperties.getAuthn().getSamlIdp().getCore().getSessionReplication().getCookie();
            return CookieUtils.buildCookieRetrievingGenerator(cookie,
                new DefaultCasCookieValueManager(cipherExecutorResolver, tenantExtractor,
                    geoLocationService, DefaultCookieSameSitePolicy.INSTANCE, cookie));
        }

        @ConditionalOnMissingBean(name = "samlIdPDistributedSessionStore")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SessionStore samlIdPDistributedSessionStore(
            final CasConfigurationProperties casProperties,
            @Qualifier("samlIdPDistributedSessionCookieGenerator")
            final CasCookieBuilder samlIdPDistributedSessionCookieGenerator,
            @Qualifier(CipherExecutor.BEAN_NAME_WEBFLOW_CIPHER_EXECUTOR)
            final CipherExecutor webflowCipherExecutor,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory) {
            val type = casProperties.getAuthn().getSamlIdp().getCore().getSessionStorageType();
            return switch (type) {
                case TICKET_REGISTRY -> new TicketRegistrySessionStore(ticketRegistry, ticketFactory, samlIdPDistributedSessionCookieGenerator);
                case BROWSER_STORAGE -> new BrowserWebStorageSessionStore(webflowCipherExecutor, "SamlIdPSessionStore");
                default -> {
                    val jeeSessionStore = new JEESessionStore();
                    jeeSessionStore.setPrefix("SamlServerSupport");
                    yield jeeSessionStore;
                }
            };
        }

        @Bean
        @ConditionalOnMissingBean(name = "samlIdPInfoEndpointContributor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnEnabledInfoContributor(value = "saml-idp", fallback = InfoContributorFallback.DISABLE)
        public InfoContributor samlIdPInfoEndpointContributor(
            final CasConfigurationProperties casProperties) {
            return new SamlIdPInfoEndpointContributor(casProperties);
        }
    }

    @Configuration(value = "SamlIdPExecutionContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlIdPExecutionContextConfiguration {
        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
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
            @Qualifier(SamlRegisteredServiceCachingMetadataResolver.BEAN_NAME)
            final SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver,
            @Qualifier("samlIdPServiceFactory")
            final ServiceFactory samlIdPServiceFactory,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            final CasConfigurationProperties casProperties,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
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
            @Qualifier(SingleSignOnParticipationStrategy.BEAN_NAME)
            final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy,
            @Qualifier("singleLogoutServiceLogoutUrlBuilder")
            final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder,
            @Qualifier("samlIdPLogoutResponseObjectBuilder")
            final Saml20ObjectBuilder samlIdPLogoutResponseObjectBuilder,
            @Qualifier("samlIdPDistributedSessionCookieGenerator")
            final CasCookieBuilder samlIdPDistributedSessionCookieGenerator,
            @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier("samlIdPDistributedSessionStore")
            final SessionStore samlIdPDistributedSessionStore,
            @Qualifier("samlProfileSamlResponseBuilder")
            final SamlProfileObjectBuilder<Response> samlProfileSamlResponseBuilder,
            @Qualifier("samlProfileSamlAttributeQueryFaultResponseBuilder")
            final SamlProfileObjectBuilder<Envelope> samlProfileSamlAttributeQueryFaultResponseBuilder,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory defaultTicketFactory,
            @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
            final PersonAttributeDao attributeRepository,
            @Qualifier("shibbolethCompatiblePersistentIdGenerator")
            final PersistentIdGenerator shibbolethCompatiblePersistentIdGenerator,
            @Qualifier("ssoPostProfileHandlerDecoders")
            final XMLMessageDecodersMap ssoPostProfileHandlerDecoders) {
            return SamlProfileHandlerConfigurationContext.builder()
                .attributeRepository(attributeRepository)
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
                .applicationContext(applicationContext)
                .samlFaultResponseBuilder(samlProfileSamlAttributeQueryFaultResponseBuilder)
                .persistentIdGenerator(shibbolethCompatiblePersistentIdGenerator)
                .build();
        }
    }
}
