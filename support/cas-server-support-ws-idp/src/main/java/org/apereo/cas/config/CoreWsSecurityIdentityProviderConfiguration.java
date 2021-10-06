package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.SecurityTokenServiceClientBuilder;
import org.apereo.cas.authentication.SecurityTokenServiceTokenFetcher;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.ticket.SecurityTokenTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.InternalTicketValidator;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.authentication.WSFederationAuthenticationServiceSelectionStrategy;
import org.apereo.cas.ws.idp.metadata.WSFederationMetadataController;
import org.apereo.cas.ws.idp.services.DefaultRelyingPartyTokenProducer;
import org.apereo.cas.ws.idp.services.WSFederationRelyingPartyTokenProducer;
import org.apereo.cas.ws.idp.services.WSFederationServiceRegistry;
import org.apereo.cas.ws.idp.services.WsFederationServicesManagerRegisteredServiceLocator;
import org.apereo.cas.ws.idp.web.WSFederationRequestConfigurationContext;
import org.apereo.cas.ws.idp.web.WSFederationValidateRequestCallbackController;
import org.apereo.cas.ws.idp.web.WSFederationValidateRequestController;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;

import java.util.HashSet;
import java.util.List;

/**
 * This is {@link CoreWsSecurityIdentityProviderConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ImportResource(locations = "classpath:META-INF/cxf/cxf.xml")
@Slf4j
@Configuration(value = "coreWsSecurityIdentityProviderConfiguration", proxyBeanMethods = false)
public class CoreWsSecurityIdentityProviderConfiguration {

    @Configuration(value = "CoreWsSecurityIdentityProviderWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CoreWsSecurityIdentityProviderWebConfiguration {
        @Bean
        public ProtocolEndpointWebSecurityConfigurer<Void> wsFederationProtocolEndpointConfigurer() {
            return new ProtocolEndpointWebSecurityConfigurer<>() {

                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of(StringUtils.prependIfMissing(WSFederationConstants.BASE_ENDPOINT_IDP, "/"), StringUtils.prependIfMissing(WSFederationConstants.BASE_ENDPOINT_STS, "/"));
                }
            };
        }
    }

    @Configuration(value = "CoreWsSecurityIdentityProviderServiceSelectionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CoreWsSecurityIdentityProviderServiceSelectionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        @ConditionalOnMissingBean(name = "wsFederationAuthenticationServiceSelectionStrategy")
        public AuthenticationServiceSelectionStrategy wsFederationAuthenticationServiceSelectionStrategy(
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new WSFederationAuthenticationServiceSelectionStrategy(servicesManager, webApplicationServiceFactory);
        }
    }

    @Configuration(value = "CoreWsSecurityIdentityProviderServiceSelectionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CoreWsSecurityIdentityProviderServiceSelectionPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "wsFederationAuthenticationServiceSelectionStrategyConfigurer")
        @Autowired
        public AuthenticationServiceSelectionStrategyConfigurer wsFederationAuthenticationServiceSelectionStrategyConfigurer(
            @Qualifier("wsFederationAuthenticationServiceSelectionStrategy")
            final AuthenticationServiceSelectionStrategy wsFederationAuthenticationServiceSelectionStrategy) {
            return plan -> plan.registerStrategy(wsFederationAuthenticationServiceSelectionStrategy);
        }
    }
    
    @Configuration(value = "CoreWsSecurityIdentityProviderServicesConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CoreWsSecurityIdentityProviderServicesConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "wsFederationServicesManagerRegisteredServiceLocator")
        public ServicesManagerRegisteredServiceLocator wsFederationServicesManagerRegisteredServiceLocator() {
            return new WsFederationServicesManagerRegisteredServiceLocator();
        }

        @Bean
        @Autowired
        public Service wsFederationCallbackService(
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
            return webApplicationServiceFactory
                .createService(WSFederationConstants.ENDPOINT_FEDERATION_REQUEST_CALLBACK);
        }

        @Bean
        @ConditionalOnMissingBean(name = "wsFederationServiceRegistryExecutionPlanConfigurer")
        @Autowired
        public ServiceRegistryExecutionPlanConfigurer wsFederationServiceRegistryExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("wsFederationCallbackService")
            final Service wsFederationCallbackService) {
            return plan -> {
                LOGGER.debug("Initializing WS Federation callback service [{}]", wsFederationCallbackService);
                val service = new RegexRegisteredService();
                service.setId(RandomUtils.nextLong());
                service.setEvaluationOrder(Ordered.HIGHEST_PRECEDENCE);
                service.setName(service.getClass().getSimpleName());
                service.setDescription("WS-Federation Authentication Request");
                service.setServiceId(wsFederationCallbackService.getId().concat(".+"));
                LOGGER.debug("Saving callback service [{}] into the registry", service.getServiceId());
                plan.registerServiceRegistry(new WSFederationServiceRegistry(applicationContext, service));
            };
        }
    }

    @Configuration(value = "CoreWsSecurityIdentityProviderTicketsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CoreWsSecurityIdentityProviderTicketsConfiguration {

        @Autowired
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "wsFederationRelyingPartyTokenProducer")
        public WSFederationRelyingPartyTokenProducer wsFederationRelyingPartyTokenProducer(
            @Qualifier("securityTokenServiceCredentialCipherExecutor")
            final CipherExecutor securityTokenServiceCredentialCipherExecutor,
            @Qualifier("securityTokenServiceClientBuilder")
            final SecurityTokenServiceClientBuilder securityTokenServiceClientBuilder,
            final CasConfigurationProperties casProperties) {
            return new DefaultRelyingPartyTokenProducer(securityTokenServiceClientBuilder,
                securityTokenServiceCredentialCipherExecutor, new HashSet<>(casProperties.getAuthn().getWsfedIdp().getSts().getCustomClaims()));
        }

        @Bean
        @ConditionalOnMissingBean(name = "wsFederationTicketValidator")
        public TicketValidator wsFederationTicketValidator(
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier("authenticationAttributeReleasePolicy")
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new InternalTicketValidator(centralAuthenticationService,
                webApplicationServiceFactory, authenticationAttributeReleasePolicy, servicesManager);
        }
    }

    @Configuration(value = "CoreWsSecurityIdentityProviderContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CoreWsSecurityIdentityProviderContextConfiguration {
        @Bean
        public WSFederationRequestConfigurationContext wsFederationConfigurationContext(
            @Qualifier("wsFederationRelyingPartyTokenProducer")
            final WSFederationRelyingPartyTokenProducer wsFederationRelyingPartyTokenProducer,
            @Qualifier("noRedirectHttpClient")
            final HttpClient httpClient,
            @Qualifier("wsFederationAuthenticationServiceSelectionStrategy")
            final AuthenticationServiceSelectionStrategy wsFederationAuthenticationServiceSelectionStrategy,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport,
            @Qualifier("securityTokenTicketFactory")
            final SecurityTokenTicketFactory securityTokenTicketFactory,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("wsFederationCallbackService")
            final Service wsFederationCallbackService,
            @Qualifier("securityTokenServiceTokenFetcher")
            final SecurityTokenServiceTokenFetcher securityTokenServiceTokenFetcher,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("ticketGrantingTicketCookieGenerator")
            final CasCookieBuilder ticketGrantingTicketCookieGenerator,
            @Qualifier("wsFederationTicketValidator")
            final TicketValidator wsFederationTicketValidator,
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            final CasConfigurationProperties casProperties) {
            return WSFederationRequestConfigurationContext.builder()
                .servicesManager(servicesManager)
                .relyingPartyTokenProducer(wsFederationRelyingPartyTokenProducer)
                .webApplicationServiceFactory(webApplicationServiceFactory)
                .casProperties(casProperties)
                .ticketValidator(wsFederationTicketValidator)
                .securityTokenServiceTokenFetcher(securityTokenServiceTokenFetcher)
                .serviceSelectionStrategy(wsFederationAuthenticationServiceSelectionStrategy)
                .httpClient(httpClient)
                .securityTokenTicketFactory(securityTokenTicketFactory)
                .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator)
                .ticketRegistry(ticketRegistry)
                .ticketRegistrySupport(ticketRegistrySupport)
                .callbackService(wsFederationCallbackService)
                .build();
        }

    }

    @Configuration(value = "CoreWsSecurityIdentityProviderControllersConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CoreWsSecurityIdentityProviderControllersConfiguration {
        @ConditionalOnMissingBean(name = "federationValidateRequestController")
        @Bean
        @Autowired
        public WSFederationValidateRequestController federationValidateRequestController(
            @Qualifier("wsFederationConfigurationContext")
            final WSFederationRequestConfigurationContext wsFederationConfigurationContext) {
            return new WSFederationValidateRequestController(wsFederationConfigurationContext);
        }

        @Autowired
        @Bean
        public WSFederationValidateRequestCallbackController federationValidateRequestCallbackController(
            @Qualifier("wsFederationConfigurationContext")
            final WSFederationRequestConfigurationContext wsFederationConfigurationContext) {
            return new WSFederationValidateRequestCallbackController(wsFederationConfigurationContext);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public WSFederationMetadataController wsFederationMetadataController(final CasConfigurationProperties casProperties) {
            return new WSFederationMetadataController(casProperties);
        }

    }
}
