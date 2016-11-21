package org.apereo.cas.ticket.registry.jwt.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.ticket.registry.jwt.factories.JwtProxyGrantingTicketFactory;
import org.apereo.cas.ticket.registry.jwt.factories.JwtProxyTicketFactory;
import org.apereo.cas.ticket.registry.jwt.factories.JwtServiceTicketFactory;
import org.apereo.cas.ticket.registry.jwt.JwtTicketCipherExecutor;
import org.apereo.cas.ticket.registry.jwt.factories.JwtTicketGrantingTicketFactory;
import org.apereo.cas.ticket.registry.jwt.JwtTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

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
    @Qualifier("grantingTicketExpirationPolicy")
    private ExpirationPolicy grantingTicketExpirationPolicy;

    @Autowired
    @Qualifier("serviceTicketExpirationPolicy")
    private ExpirationPolicy serviceTicketExpirationPolicy;

    @Autowired
    @Qualifier("proxyTicketExpirationPolicy")
    private ExpirationPolicy proxyTicketExpirationPolicy;

    @Autowired
    @Qualifier("uniqueIdGeneratorsMap")
    private Map uniqueIdGeneratorsMap;

    @Autowired
    @Qualifier("ticketGrantingTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator ticketGrantingTicketUniqueIdGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean(name = {"jwtTicketRegistry", "ticketRegistry"})
    @RefreshScope
    public TicketRegistry jwtTicketRegistry() {
        final JwtTicketRegistry r = new JwtTicketRegistry();
        r.setCipherExecutor(protocolTicketCipherExecutor());
        return r;
    }

    @Bean
    public ProxyGrantingTicketFactory defaultProxyGrantingTicketFactory() {
        final JwtProxyGrantingTicketFactory f = new JwtProxyGrantingTicketFactory();
        f.setTicketGrantingTicketExpirationPolicy(grantingTicketExpirationPolicy);
        f.setTicketGrantingTicketUniqueTicketIdGenerator(ticketGrantingTicketUniqueIdGenerator);
        f.setCipherExecutor(protocolTicketCipherExecutor());
        return f;
    }

    @Bean
    public ProxyTicketFactory defaultProxyTicketFactory() {
        final JwtProxyTicketFactory f = new JwtProxyTicketFactory();
        f.setProxyTicketExpirationPolicy(proxyTicketExpirationPolicy);
        f.setUniqueTicketIdGeneratorsForService(uniqueIdGeneratorsMap);
        f.setCipherExecutor(protocolTicketCipherExecutor());
        return f;
    }

    @Bean
    public ServiceTicketFactory defaultServiceTicketFactory() {
        final JwtServiceTicketFactory f = new JwtServiceTicketFactory();
        f.setServiceTicketExpirationPolicy(serviceTicketExpirationPolicy);
        f.setUniqueTicketIdGeneratorsForService(uniqueIdGeneratorsMap);
        f.setTrackMostRecentSession(casProperties.getTicket().getTgt().isOnlyTrackMostRecentSession());
        f.setCipherExecutor(protocolTicketCipherExecutor());
        return f;
    }

    @Bean
    public TicketGrantingTicketFactory defaultTicketGrantingTicketFactory() {
        final JwtTicketGrantingTicketFactory f = new JwtTicketGrantingTicketFactory();
        f.setTicketGrantingTicketExpirationPolicy(grantingTicketExpirationPolicy);
        f.setTicketGrantingTicketUniqueTicketIdGenerator(ticketGrantingTicketUniqueIdGenerator);
        f.setCipherExecutor(protocolTicketCipherExecutor());
        return f;
    }

    @Bean
    public CipherExecutor protocolTicketCipherExecutor() {
        final CryptographyProperties crypto = casProperties.getTicket().getRegistry().getJwt().getCrypto();
        return new JwtTicketCipherExecutor(crypto.getEncryption().getKey(), crypto.getSigning().getKey());
    }

}
