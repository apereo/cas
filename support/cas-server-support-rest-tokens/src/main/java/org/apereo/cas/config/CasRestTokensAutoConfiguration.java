package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.rest.factory.TicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.plan.ServiceTicketResourceEntityResponseFactoryConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.token.TokenTicketBuilder;
import org.apereo.cas.tokens.JwtServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.tokens.JwtTicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasRestTokensAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.RestProtocol, module = "tokens")
@AutoConfiguration
public class CasRestTokensAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketGrantingTicketResourceEntityResponseFactory ticketGrantingTicketResourceEntityResponseFactory(
        @Qualifier(TokenTicketBuilder.BEAN_NAME)
        final TokenTicketBuilder tokenTicketBuilder) {
        return new JwtTicketGrantingTicketResourceEntityResponseFactory(tokenTicketBuilder);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ServiceTicketResourceEntityResponseFactoryConfigurer restTokenServiceTicketResourceEntityResponseFactoryConfigurer(
        @Qualifier(CentralAuthenticationService.BEAN_NAME)
        final CentralAuthenticationService centralAuthenticationService,
        @Qualifier(TokenTicketBuilder.BEAN_NAME)
        final TokenTicketBuilder tokenTicketBuilder,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(TicketRegistrySupport.BEAN_NAME)
        final TicketRegistrySupport ticketRegistrySupport) {
        return plan -> plan.registerFactory(new JwtServiceTicketResourceEntityResponseFactory(centralAuthenticationService, tokenTicketBuilder, ticketRegistrySupport, servicesManager));
    }
}
