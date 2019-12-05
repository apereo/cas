package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TransientSessionTicketImpl;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    @Qualifier("casSimpleMultifactorAuthenticationTicketExpirationPolicy")
    private ObjectProvider<ExpirationPolicyBuilder> casSimpleMultifactorAuthenticationTicketExpirationPolicy;

    @Override
    public void configureTicketCatalog(final TicketCatalog plan) {
        LOGGER.debug("Registering ticket definitions...");
        val definition = buildTicketDefinition(plan, CasSimpleMultifactorAuthenticationTicketFactory.PREFIX,
            TransientSessionTicketImpl.class, Ordered.HIGHEST_PRECEDENCE);
        val properties = definition.getProperties();
        properties.setStorageName("casSimpleMultifactorAuthenticationTicketsCache");
        val timeToLive = casSimpleMultifactorAuthenticationTicketExpirationPolicy.getObject().buildTicketExpirationPolicy().getTimeToLive();
        properties.setStorageTimeout(timeToLive);
        registerTicketDefinition(plan, definition);
    }
}
