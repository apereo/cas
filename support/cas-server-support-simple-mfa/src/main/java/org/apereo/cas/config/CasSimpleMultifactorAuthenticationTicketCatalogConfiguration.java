package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketImpl;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketCatalog;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * This is {@link CasSimpleMultifactorAuthenticationTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "casSimpleMultifactorAuthenticationTicketCatalogConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasSimpleMultifactorAuthenticationTicketCatalogConfiguration extends BaseTicketCatalogConfigurer {
    private final ExpirationPolicyBuilder casSimpleMultifactorAuthenticationTicketExpirationPolicy;

    public CasSimpleMultifactorAuthenticationTicketCatalogConfiguration(
        @Qualifier("casSimpleMultifactorAuthenticationTicketExpirationPolicy")
        final ExpirationPolicyBuilder policyBuilder) {
        this.casSimpleMultifactorAuthenticationTicketExpirationPolicy = policyBuilder;
    }

    @Override
    public void configureTicketCatalog(final TicketCatalog plan, final CasConfigurationProperties casProperties) {
        LOGGER.trace("Registering ticket definitions...");
        val definition = buildTicketDefinition(plan, CasSimpleMultifactorAuthenticationTicket.PREFIX, CasSimpleMultifactorAuthenticationTicketImpl.class, Ordered.HIGHEST_PRECEDENCE);
        val properties = definition.getProperties();
        properties.setStorageName("casSimpleMultifactorAuthenticationTicketsCache");
        val timeToLive = casSimpleMultifactorAuthenticationTicketExpirationPolicy.buildTicketExpirationPolicy().getTimeToLive();
        properties.setStorageTimeout(timeToLive);
        registerTicketDefinition(plan, definition);
    }
}
