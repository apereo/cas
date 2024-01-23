package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.TicketCatalogConfigurer;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20CodeCompactor;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasOAuth20TicketsAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OAuth)
@AutoConfiguration
public class CasOAuth20TicketsAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oauth20TicketCatalogConfigurer")
    public TicketCatalogConfigurer oauth20TicketCatalogConfigurer() {
        return new OAuth20TicketCatalogConfigurer();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oauth20CodeTicketCompactor")
    public TicketCompactor<OAuth20Code> oauth20CodeTicketCompactor(
        @Qualifier(PrincipalFactory.BEAN_NAME) final PrincipalFactory principalFactory,
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY) final ServiceFactory serviceFactory,
        @Qualifier(TicketFactory.BEAN_NAME) final ObjectProvider<TicketFactory> ticketFactory) {
        return new OAuth20CodeCompactor(ticketFactory, serviceFactory, principalFactory);
    }
}
