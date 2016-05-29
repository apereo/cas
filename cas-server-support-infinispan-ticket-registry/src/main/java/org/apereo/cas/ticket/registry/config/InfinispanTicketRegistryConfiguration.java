package org.apereo.cas.ticket.registry.config;

import org.apereo.cas.ticket.registry.InfinispanTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link InfinispanTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("infinispanTicketRegistryConfiguration")
public class InfinispanTicketRegistryConfiguration {
    
    @Bean
    public TicketRegistry infinispanTicketRegistry() {
        return new InfinispanTicketRegistry();
    }
}
