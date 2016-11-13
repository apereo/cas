package org.apereo.cas.ticket.registry.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.registry.JwtTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link JwtTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("infinispanTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JwtTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean(name = {"jwtTicketRegistry", "ticketRegistry"})
    @RefreshScope
    public TicketRegistry jwtTicketRegistry() {
        final JwtTicketRegistry r = new JwtTicketRegistry();
        r.setCipherExecutor(Beans.newTicketRegistryCipherExecutor(casProperties.getTicket().getRegistry().getJwt().getCrypto()));
        return r;
    }
}
