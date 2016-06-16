package org.apereo.cas.ticket.registry.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.ticket.registry.InfinispanTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.infinispan.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class InfinispanTicketRegistryConfiguration {
    
    @Nullable
    @Autowired(required = false)
    @Qualifier("ticketCipherExecutor")
    private CipherExecutor<byte[], byte[]> cipherExecutor;

    @Autowired
    @Qualifier("infinispanTicketsCache")
    private Cache infinispanTicketsCache;

    @Bean
    public TicketRegistry infinispanTicketRegistry() {
        final InfinispanTicketRegistry r = new InfinispanTicketRegistry();
        r.setCipherExecutor(cipherExecutor);
        r.setCache(infinispanTicketsCache);
        return r;
    }
}
