package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.SecurityTokenServiceClientBuilder;
import org.apereo.cas.authentication.SecurityTokenServiceTokenFetcher;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.SecurityTokenTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.authentication.WSFederationAuthenticationServiceSelectionStrategy;
import org.apereo.cas.ws.idp.metadata.WSFederationMetadataController;
import org.apereo.cas.ws.idp.services.DefaultRelyingPartyTokenProducer;
import org.apereo.cas.ws.idp.services.WSFederationRelyingPartyTokenProducer;
import org.apereo.cas.ws.idp.services.WSFederationServiceRegistry;
import org.apereo.cas.ws.idp.web.WSFederationRequestConfigurationContext;
import org.apereo.cas.ws.idp.web.WSFederationValidateRequestCallbackController;
import org.apereo.cas.ws.idp.web.WSFederationValidateRequestController;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.Ordered;

import java.util.HashSet;

/**
 * This is {@link CoreWsSecurityIdentityProviderConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("coreWsSecurityIdentityProviderConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ImportResource(locations = "classpath:META-INF/cxf/cxf.xml")
@Slf4j
public class CoreWsSecurityIdentityProviderConfiguration {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("casClientTicketValidator")
    private ObjectProvider<AbstractUrlBasedTicketValidator> casClientTicketValidator;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CasCookieBuilder> ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private ObjectProvider<HttpClient> httpClient;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ObjectProvider<ServiceFactory> webApplicationServiceFactory;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("securityTokenTicketFactory")
    private ObjectProvider<SecurityTokenTicketFactory> securityTokenTicketFactory;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("securityTokenServiceTokenFetcher")
    private ObjectProvider<SecurityTokenServiceTokenFetcher> securityTokenServiceTokenFetcher;

    @ConditionalOnMissingBean(name = "federationValidateRequestController")
    @Bean
    public WSFederationValidateRequestController federationValidateRequestController() {
        return new WSFederationValidateRequestController(getConfigurationContext().build());
    }

    @Autowired
    @Bean
    public WSFederationValidateRequestCallbackController federationValidateRequestCallbackController(
        @Qualifier("wsFederationRelyingPartyTokenProducer") final WSFederationRelyingPartyTokenProducer wsFederationRelyingPartyTokenProducer) {
        val context = getConfigurationContext()
            .relyingPartyTokenProducer(wsFederationRelyingPartyTokenProducer)
            .build();
        return new WSFederationValidateRequestCallbackController(context);
    }

    @Bean
    public Service wsFederationCallbackService() {
        return webApplicationServiceFactory.getObject().createService(WSFederationConstants.ENDPOINT_FEDERATION_REQUEST_CALLBACK);
    }

    @Bean
    @RefreshScope
    public WSFederationMetadataController wsFederationMetadataController() {
        return new WSFederationMetadataController(casProperties);
    }

    @Autowired
    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "wsFederationRelyingPartyTokenProducer")
    public WSFederationRelyingPartyTokenProducer wsFederationRelyingPartyTokenProducer(
        @Qualifier("securityTokenServiceCredentialCipherExecutor") final CipherExecutor securityTokenServiceCredentialCipherExecutor,
        @Qualifier("securityTokenServiceClientBuilder") final SecurityTokenServiceClientBuilder securityTokenServiceClientBuilder) {
        return new DefaultRelyingPartyTokenProducer(securityTokenServiceClientBuilder,
            securityTokenServiceCredentialCipherExecutor,
            new HashSet<>(casProperties.getAuthn().getWsfedIdp().getSts().getCustomClaims()));
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "wsFederationAuthenticationServiceSelectionStrategy")
    public AuthenticationServiceSelectionStrategy wsFederationAuthenticationServiceSelectionStrategy() {
        return new WSFederationAuthenticationServiceSelectionStrategy(webApplicationServiceFactory.getObject());
    }

    @Bean
    public AuthenticationServiceSelectionStrategyConfigurer wsFederationAuthenticationServiceSelectionStrategyConfigurer() {
        return plan -> plan.registerStrategy(wsFederationAuthenticationServiceSelectionStrategy());
    }

    @Bean
    @ConditionalOnMissingBean(name = "wsFederationServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer wsFederationServiceRegistryExecutionPlanConfigurer() {
        return plan -> {
            val callbackService = wsFederationCallbackService();
            LOGGER.debug("Initializing WS Federation callback service [{}]", callbackService);
            val service = new RegexRegisteredService();
            service.setId(RandomUtils.nextLong());
            service.setEvaluationOrder(Ordered.HIGHEST_PRECEDENCE);
            service.setName(service.getClass().getSimpleName());
            service.setDescription("WS-Federation Authentication Request");
            service.setServiceId(callbackService.getId().concat(".+"));
            LOGGER.debug("Saving callback service [{}] into the registry", service);
            plan.registerServiceRegistry(new WSFederationServiceRegistry(applicationContext, service));
        };
    }

    private WSFederationRequestConfigurationContext.WSFederationRequestConfigurationContextBuilder getConfigurationContext() {
        return WSFederationRequestConfigurationContext.builder()
            .servicesManager(servicesManager.getObject())
            .webApplicationServiceFactory(webApplicationServiceFactory.getObject())
            .casProperties(casProperties)
            .ticketValidator(casClientTicketValidator.getObject())
            .securityTokenServiceTokenFetcher(securityTokenServiceTokenFetcher.getObject())
            .serviceSelectionStrategy(wsFederationAuthenticationServiceSelectionStrategy())
            .httpClient(httpClient.getObject())
            .securityTokenTicketFactory(securityTokenTicketFactory.getObject())
            .ticketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator.getObject())
            .ticketRegistry(ticketRegistry.getObject())
            .ticketRegistrySupport(ticketRegistrySupport.getObject())
            .callbackService(wsFederationCallbackService());
    }
}
