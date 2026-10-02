package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketCompactor;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketImpl;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketCatalogConfigurer;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.StatelessTicketRegistry;
import org.apereo.cas.ticket.registry.compact.TicketCompactor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;

/**
 * This is {@link CasSimpleMultifactorAuthenticationTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SimpleMFA)
@Configuration(value = "CasSimpleMultifactorAuthenticationTicketCatalogConfiguration", proxyBeanMethods = false)
class CasSimpleMultifactorAuthenticationTicketCatalogConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketCatalogConfigurer casSimpleMultifactorAuthenticationTicketCatalogConfigurer(
        @Qualifier("casSimpleMultifactorAuthenticationTicketExpirationPolicy")
        final ExpirationPolicyBuilder casSimpleMultifactorAuthenticationTicketExpirationPolicy) {
        return new BaseTicketCatalogConfigurer() {
            @Override
            public void configureTicketCatalog(final TicketCatalog plan, final CasConfigurationProperties casProperties) {
                LOGGER.trace("Registering ticket definitions...");
                val definition = buildTicketDefinition(plan, CasSimpleMultifactorAuthenticationTicket.PREFIX,
                    CasSimpleMultifactorAuthenticationTicket.class, CasSimpleMultifactorAuthenticationTicketImpl.class, Ordered.HIGHEST_PRECEDENCE);
                val properties = definition.getProperties();
                properties.setStorageName("casSimpleMultifactorAuthenticationTicketsCache");
                val timeToLive = casSimpleMultifactorAuthenticationTicketExpirationPolicy.buildTicketExpirationPolicy().getTimeToLive();
                properties.setStorageTimeout(timeToLive);
                registerTicketDefinition(plan, definition);
            }
        };
    }

    @Configuration(value = "CasSimpleMultifactorAuthenticationStatelessTicketsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "stateless")
    @ConditionalOnClass(StatelessTicketRegistry.class)
    static class CasSimpleMultifactorAuthenticationStatelessTicketsConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationTicketCompactor")
        public TicketCompactor<CasSimpleMultifactorAuthenticationTicket> casSimpleMultifactorAuthenticationTicketCompactor(
            @Qualifier(TicketFactory.BEAN_NAME)
            final ObjectProvider<TicketFactory> ticketFactory,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory serviceFactory,
            @Qualifier("casSimpleMultifactorPrincipalFactory")
            final PrincipalFactory casSimpleMultifactorPrincipalFactory) {
            return new CasSimpleMultifactorAuthenticationTicketCompactor(ticketFactory, serviceFactory, casSimpleMultifactorPrincipalFactory);
        }
    }
}
