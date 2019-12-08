package org.apereo.cas.ticket.registry.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.infinispan.InfinispanProperties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.InfinispanTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CoreTicketUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    @RefreshScope
    public TicketRegistry ticketRegistry() {
        val span = casProperties.getTicket().getRegistry().getInfinispan();
        val r = new InfinispanTicketRegistry(getCache(span));
        r.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(span.getCrypto(), "infinispan"));
        return r;
    }

    private Cache<String, Ticket> getCache(final InfinispanProperties span) {
        val cacheName = span.getCacheName();
        if (StringUtils.isBlank(cacheName)) {
            return cacheManager().getCache();
        }
        return cacheManager().getCache(cacheName);
    }

    @Bean
    @SneakyThrows
    public EmbeddedCacheManager cacheManager() {
        val loc = casProperties.getTicket().getRegistry().getInfinispan().getConfigLocation();
        return new DefaultCacheManager(loc.getInputStream());
    }
}
