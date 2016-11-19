package org.apereo.cas.ticket.registry.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.ticket.registry.JwtProxyGrantingTicketFactory;
import org.apereo.cas.ticket.registry.JwtProxyTicketFactory;
import org.apereo.cas.ticket.registry.JwtServiceTicketFactory;
import org.apereo.cas.ticket.registry.JwtTicketGrantingTicketFactory;
import org.apereo.cas.ticket.registry.JwtTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.EncodingUtils;
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
    @Qualifier("protocolTicketCipherExecutor")
    private CipherExecutor protocolTicketCipherExecutor;

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
        r.setCipherExecutor(Beans.newTicketRegistryCipherExecutor(casProperties.getTicket().getRegistry().getJwt().getCrypto()));
        return r;
    }

    @Bean
    public ProxyGrantingTicketFactory defaultProxyGrantingTicketFactory() {
        final JwtProxyGrantingTicketFactory f = new JwtProxyGrantingTicketFactory();
        f.setTicketGrantingTicketExpirationPolicy(grantingTicketExpirationPolicy);
        f.setTicketGrantingTicketUniqueTicketIdGenerator(ticketGrantingTicketUniqueIdGenerator);
        f.setCipherExecutor(protocolTicketCipherExecutor);
        return f;
    }

    @Bean
    public ProxyTicketFactory defaultProxyTicketFactory() {
        final JwtProxyTicketFactory f = new JwtProxyTicketFactory();
        f.setProxyTicketExpirationPolicy(proxyTicketExpirationPolicy);
        f.setUniqueTicketIdGeneratorsForService(uniqueIdGeneratorsMap);
        f.setCipherExecutor(protocolTicketCipherExecutor);
        return f;
    }

    @Bean
    public ServiceTicketFactory defaultServiceTicketFactory() {
        final JwtServiceTicketFactory f = new JwtServiceTicketFactory();
        f.setServiceTicketExpirationPolicy(serviceTicketExpirationPolicy);
        f.setUniqueTicketIdGeneratorsForService(uniqueIdGeneratorsMap);
        f.setTrackMostRecentSession(casProperties.getTicket().getTgt().isOnlyTrackMostRecentSession());
        f.setCipherExecutor(protocolTicketCipherExecutor);
        return f;
    }

    @Bean
    public TicketGrantingTicketFactory defaultTicketGrantingTicketFactory() {
        final JwtTicketGrantingTicketFactory f = new JwtTicketGrantingTicketFactory(jwtSigningEncryptionKeyPair());
        f.setTicketGrantingTicketExpirationPolicy(grantingTicketExpirationPolicy);
        f.setTicketGrantingTicketUniqueTicketIdGenerator(ticketGrantingTicketUniqueIdGenerator);
        f.setCipherExecutor(protocolTicketCipherExecutor);
        return f;
    }

    @Bean
    public Pair<String, String> jwtSigningEncryptionKeyPair() {
        final CryptographyProperties crypto = casProperties.getTicket().getRegistry().getJwt().getCrypto();

        String signing = crypto.getSigning().getKey();
        if (StringUtils.isBlank(signing)) {
            signing = EncodingUtils.generateJsonWebKey(crypto.getSigning().getKeySize());
        }

        String encryption = crypto.getEncryption().getKey();
        if (StringUtils.isBlank(encryption)) {
            encryption = EncodingUtils.generateJsonWebKey(crypto.getEncryption().getKeySize());
        }
        return Pair.of(signing, encryption);
    }
}
