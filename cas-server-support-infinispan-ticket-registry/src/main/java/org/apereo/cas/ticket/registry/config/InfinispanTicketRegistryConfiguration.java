package org.apereo.cas.ticket.registry.config;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.InfinispanTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nullable;

/**
 * This is {@link InfinispanTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("infinispanTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class InfinispanTicketRegistryConfiguration {

    @Nullable
    @Autowired(required = false)
    @Qualifier("ticketCipherExecutor")
    private CipherExecutor<byte[], byte[]> cipherExecutor;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "ticketRegistry")
    @Bean(name = {"infinispanTicketRegistry", "ticketRegistry"})
    public TicketRegistry infinispanTicketRegistry() {
        final InfinispanTicketRegistry r = new InfinispanTicketRegistry();
        r.setCipherExecutor(cipherExecutor);
        final String cacheName = casProperties.getTicket().getRegistry().getInfinispan().getCacheName();
        if (StringUtils.isBlank(cacheName)) {
            r.setCache(cacheManager().getCache());
        } else {
            r.setCache(cacheManager().getCache(cacheName));
        }
        return r;
    }

    @Bean
    public EmbeddedCacheManager cacheManager() {
        try {
            return new DefaultCacheManager(casProperties.getTicket()
                    .getRegistry().getInfinispan().getConfigLocation().getFilename());
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
