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
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.jwt.cipher.JwtTicketCipherExecutor;
import org.apereo.cas.ticket.registry.jwt.factories.JwtProxyGrantingTicketFactory;
import org.apereo.cas.ticket.registry.jwt.factories.JwtProxyTicketFactory;
import org.apereo.cas.ticket.registry.jwt.factories.JwtServiceTicketFactory;
import org.apereo.cas.ticket.registry.jwt.factories.JwtTicketGrantingTicketFactory;
import org.apereo.cas.util.cipher.Base64CipherExecutor;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
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
@AutoConfigureBefore(CasCookieConfiguration.class)
public class JwtTicketRegistryConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTicketRegistryConfiguration.class);

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
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("ticketGrantingTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator ticketGrantingTicketUniqueIdGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean(name = {"jwtTicketRegistry", "ticketRegistry"})
    @RefreshScope
    public TicketRegistry jwtTicketRegistry() {
        final JwtTicketRegistry r = new JwtTicketRegistry(ticketGrantingTicketCookieGenerator);
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

    @Bean
    public CipherExecutor cookieCipherExecutor() {
        LOGGER.info("Ticket-granting cookie encryption/signing is handled internally via JWTed tickets. "
                + "Therefore, ticket-granting cookie cipher execution is turned off automatically.");
        return Base64CipherExecutor.getInstance();
    }
}
