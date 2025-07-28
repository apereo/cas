package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.session.TicketRegistrySessionRepository;
import org.apereo.cas.session.TransientTicketSessionIdGenerator;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.session.SessionIdGenerator;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import java.time.Duration;

/**
 * This is {@link CasTicketRegistrySessionAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SessionManagement, module = "ticket-registry")
@AutoConfiguration
@EnableSpringHttpSession
public class CasTicketRegistrySessionAutoConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "ticketRegistrySessionRepository")
    public SessionRepository sessionRepository(
        @Qualifier("ticketRegistrySessionIdGenerator")
        final SessionIdGenerator ticketRegistrySessionIdGenerator,
        @Qualifier(TicketFactory.BEAN_NAME)
        final TicketFactory ticketFactory,
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry) {
        val repository = new TicketRegistrySessionRepository(ticketRegistry, ticketFactory);
        val factory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
        repository.setSessionIdGenerator(ticketRegistrySessionIdGenerator);
        val expirationPolicy = factory.getExpirationPolicyBuilder().buildTicketExpirationPolicy();
        repository.setDefaultMaxInactiveInterval(Duration.ofSeconds(expirationPolicy.getTimeToLive()));
        return repository;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "ticketRegistrySessionIdGenerator")
    public SessionIdGenerator ticketRegistrySessionIdGenerator(
        @Qualifier(TicketFactory.BEAN_NAME)
        final TicketFactory ticketFactory) {
        val factory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
        return new TransientTicketSessionIdGenerator(factory);
    }
}
