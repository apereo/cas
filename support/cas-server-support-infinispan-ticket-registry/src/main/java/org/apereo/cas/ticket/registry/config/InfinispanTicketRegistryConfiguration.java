package org.apereo.cas.ticket.registry.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.InfinispanTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CoreTicketUtils;

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
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link InfinispanTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "infinispanTicketRegistryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class InfinispanTicketRegistryConfiguration {

    private static Cache<String, Ticket> getCache(final String cacheName,
                                                  final EmbeddedCacheManager cacheManager) throws Exception {
        if (StringUtils.isBlank(cacheName)) {
            return cacheManager.getCache();
        }
        return cacheManager.getCache(cacheName);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public TicketRegistry ticketRegistry(final CasConfigurationProperties casProperties,
                                         final EmbeddedCacheManager cacheManager) throws Exception {
        val span = casProperties.getTicket().getRegistry().getInfinispan();
        val r = new InfinispanTicketRegistry(getCache(span.getCacheName(), cacheManager));
        r.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(span.getCrypto(), "infinispan"));
        return r;
    }

    @Bean
    @Autowired
    public EmbeddedCacheManager cacheManager(final CasConfigurationProperties casProperties) throws Exception {
        val loc = casProperties.getTicket().getRegistry().getInfinispan().getConfigLocation();
        return new DefaultCacheManager(loc.getInputStream());
    }
}
