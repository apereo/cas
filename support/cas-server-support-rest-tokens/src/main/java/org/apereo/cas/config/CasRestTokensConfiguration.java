package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.factory.TicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.plan.ServiceTicketResourceEntityResponseFactoryConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.token.TokenTicketBuilder;
import org.apereo.cas.tokens.JwtServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.tokens.JwtTicketGrantingTicketResourceEntityResponseFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasRestTokensConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "casRestTokensConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasRestTokensConfiguration {

    @Bean
    public TicketGrantingTicketResourceEntityResponseFactory ticketGrantingTicketResourceEntityResponseFactory(
        @Qualifier("tokenTicketBuilder")
        final TokenTicketBuilder tokenTicketBuilder) {
        return new JwtTicketGrantingTicketResourceEntityResponseFactory(tokenTicketBuilder);
    }

    @Bean
    public ServiceTicketResourceEntityResponseFactoryConfigurer restTokenServiceTicketResourceEntityResponseFactoryConfigurer(
        @Qualifier(CentralAuthenticationService.BEAN_NAME)
        final CentralAuthenticationService centralAuthenticationService,
        @Qualifier("tokenTicketBuilder")
        final TokenTicketBuilder tokenTicketBuilder,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(TicketRegistrySupport.BEAN_NAME)
        final TicketRegistrySupport ticketRegistrySupport) {
        return plan -> plan.registerFactory(new JwtServiceTicketResourceEntityResponseFactory(centralAuthenticationService, tokenTicketBuilder, ticketRegistrySupport, servicesManager));
    }
}
