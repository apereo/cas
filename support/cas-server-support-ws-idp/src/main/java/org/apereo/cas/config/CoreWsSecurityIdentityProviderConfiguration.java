package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.SecurityTokenServiceClientBuilder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.SecurityTokenTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.ws.idp.authentication.WSFederationAuthenticationServiceSelectionStrategy;
import org.apereo.cas.ws.idp.metadata.WSFederationMetadataController;
import org.apereo.cas.ws.idp.services.DefaultRelyingPartyTokenProducer;
import org.apereo.cas.ws.idp.services.WSFederationRelyingPartyTokenProducer;
import org.apereo.cas.ws.idp.web.WSFederationValidateRequestCallbackController;
import org.apereo.cas.ws.idp.web.WSFederationValidateRequestController;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Lazy;

/**
 * This is {@link CoreWsSecurityIdentityProviderConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("coreWsSecurityIdentityProviderConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ImportResource(locations = {"classpath:META-INF/cxf/cxf.xml"})
public class CoreWsSecurityIdentityProviderConfiguration implements AuthenticationServiceSelectionStrategyConfigurer {

    @Autowired
    @Qualifier("casClientTicketValidator")
    private AbstractUrlBasedTicketValidator casClientTicketValidator;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory webApplicationServiceFactory;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("securityTokenTicketFactory")
    private SecurityTokenTicketFactory securityTokenTicketFactory;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Lazy
    @Bean
    public WSFederationValidateRequestController federationValidateRequestController() {
        return new WSFederationValidateRequestController(servicesManager,
            webApplicationServiceFactory, casProperties, wsFederationAuthenticationServiceSelectionStrategy(),
            httpClient, securityTokenTicketFactory, ticketRegistry, ticketGrantingTicketCookieGenerator,
            ticketRegistrySupport);
    }

    @Lazy
    @Autowired
    @Bean
    public WSFederationValidateRequestCallbackController federationValidateRequestCallbackController(
        @Qualifier("wsFederationRelyingPartyTokenProducer") final WSFederationRelyingPartyTokenProducer wsFederationRelyingPartyTokenProducer) {
        return new WSFederationValidateRequestCallbackController(servicesManager,
            webApplicationServiceFactory, casProperties, wsFederationRelyingPartyTokenProducer,
            wsFederationAuthenticationServiceSelectionStrategy(),
            httpClient, securityTokenTicketFactory, ticketRegistry,
            ticketGrantingTicketCookieGenerator,
            ticketRegistrySupport, casClientTicketValidator);
    }

    @Lazy
    @Bean
    @RefreshScope
    public WSFederationMetadataController wsFederationMetadataController() {
        return new WSFederationMetadataController(casProperties);
    }

    @Lazy
    @Autowired
    @Bean
    public WSFederationRelyingPartyTokenProducer wsFederationRelyingPartyTokenProducer(
        @Qualifier("securityTokenServiceCredentialCipherExecutor") final CipherExecutor securityTokenServiceCredentialCipherExecutor,
        @Qualifier("securityTokenServiceClientBuilder") final SecurityTokenServiceClientBuilder securityTokenServiceClientBuilder) {
        return new DefaultRelyingPartyTokenProducer(securityTokenServiceClientBuilder, securityTokenServiceCredentialCipherExecutor);
    }

    @Bean
    @RefreshScope
    public AuthenticationServiceSelectionStrategy wsFederationAuthenticationServiceSelectionStrategy() {
        return new WSFederationAuthenticationServiceSelectionStrategy(webApplicationServiceFactory);
    }

    @Override
    public void configureAuthenticationServiceSelectionStrategy(final AuthenticationServiceSelectionPlan plan) {
        plan.registerStrategy(wsFederationAuthenticationServiceSelectionStrategy());
    }
}
