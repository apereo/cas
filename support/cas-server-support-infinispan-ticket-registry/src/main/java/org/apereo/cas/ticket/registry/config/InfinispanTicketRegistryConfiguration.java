package org.apereo.cas.ticket.registry.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.infinispan.InfinispanProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.InfinispanTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * This is {@link InfinispanTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("infinispanTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class InfinispanTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public TicketRegistry ticketRegistry() {
        final InfinispanProperties span = casProperties.getTicket().getRegistry().getInfinispan();
        final InfinispanTicketRegistry r = new InfinispanTicketRegistry(getCache(span));
        r.setCipherExecutor(Beans.newTicketRegistryCipherExecutor(span.getCrypto(), "infinispan"));
        return r;
    }

    private Cache<String, Ticket> getCache(final InfinispanProperties span) {
        final String cacheName = span.getCacheName();
        if (StringUtils.isBlank(cacheName)) {
            return cacheManager().getCache();
        }
        return cacheManager().getCache(cacheName);
    }

    @Bean
    public EmbeddedCacheManager cacheManager() {
        try {
            final Resource loc = casProperties.getTicket().getRegistry().getInfinispan().getConfigLocation();
            return new DefaultCacheManager(loc.getInputStream());
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
