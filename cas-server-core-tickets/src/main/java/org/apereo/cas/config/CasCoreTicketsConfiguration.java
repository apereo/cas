package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.DefaultProxyGrantingTicketFactory;
import org.apereo.cas.ticket.DefaultProxyTicketFactory;
import org.apereo.cas.ticket.DefaultServiceTicketFactory;
import org.apereo.cas.ticket.DefaultTicketFactory;
import org.apereo.cas.ticket.DefaultTicketGrantingTicketFactory;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.ticket.proxy.support.Cas10ProxyHandler;
import org.apereo.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.DefaultTicketRegistrySupport;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.ticket.registry.support.LockingStrategy;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.support.ThrottledUseAndTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.support.TimeoutExpirationPolicy;
import org.apereo.cas.util.HostNameBasedUniqueTicketIdGenerator;
import org.apereo.cas.util.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link CasCoreTicketsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreTicketsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
public class CasCoreTicketsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreTicketsConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    @Qualifier("logoutManager")
    private LogoutManager logoutManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;
    
    @Autowired
    @Qualifier("supportsTrustStoreSslSocketFactoryHttpClient")
    private HttpClient httpClient;
    
    @Bean
    public ProxyGrantingTicketFactory defaultProxyGrantingTicketFactory() {
        final DefaultProxyGrantingTicketFactory f = new DefaultProxyGrantingTicketFactory();
        f.setTicketGrantingTicketExpirationPolicy(grantingTicketExpirationPolicy());
        f.setTicketGrantingTicketUniqueTicketIdGenerator(ticketGrantingTicketUniqueIdGenerator());
        return f;
    }

    @RefreshScope
    @Bean
    public ProxyTicketFactory defaultProxyTicketFactory() {
        final DefaultProxyTicketFactory f = new DefaultProxyTicketFactory();
        f.setProxyTicketExpirationPolicy(proxyTicketExpirationPolicy());
        f.setUniqueTicketIdGeneratorsForService(uniqueIdGeneratorsMap());
        return f;
    }

    @Bean
    public DefaultServiceTicketFactory defaultServiceTicketFactory() {
        final DefaultServiceTicketFactory f = new DefaultServiceTicketFactory();
        f.setServiceTicketExpirationPolicy(serviceTicketExpirationPolicy());
        f.setUniqueTicketIdGeneratorsForService(uniqueIdGeneratorsMap());
        f.setTrackMostRecentSession(casProperties.getTicket().getTgt().isOnlyTrackMostRecentSession());
        return f;
    }

    @Bean
    public DefaultTicketFactory defaultTicketFactory() {
        final DefaultTicketFactory f = new DefaultTicketFactory();
        f.setProxyGrantingTicketFactory(defaultProxyGrantingTicketFactory());
        f.setTicketGrantingTicketFactory(defaultTicketGrantingTicketFactory());
        f.setServiceTicketFactory(defaultServiceTicketFactory());
        f.setProxyTicketFactory(defaultProxyTicketFactory());
        return f;
    }

    @Bean
    public DefaultTicketGrantingTicketFactory defaultTicketGrantingTicketFactory() {
        final DefaultTicketGrantingTicketFactory f = new DefaultTicketGrantingTicketFactory();
        f.setTicketGrantingTicketExpirationPolicy(grantingTicketExpirationPolicy());
        f.setTicketGrantingTicketUniqueTicketIdGenerator(ticketGrantingTicketUniqueIdGenerator());
        return f;
    }

    @Bean
    public ProxyHandler proxy10Handler() {
        return new Cas10ProxyHandler();
    }

    @Bean
    public ProxyHandler proxy20Handler() {
        final Cas20ProxyHandler h = new Cas20ProxyHandler();
        h.setHttpClient(httpClient);
        h.setUniqueTicketIdGenerator(proxy20TicketUniqueIdGenerator());
        return h;
    }

    @RefreshScope
    @Bean(name = {"defaultTicketRegistry", "ticketRegistry"})
    public TicketRegistry defaultTicketRegistry() {
        final DefaultTicketRegistry r = new DefaultTicketRegistry(
                casProperties.getTicket().getRegistry().getInMemory().getInitialCapacity(),
                casProperties.getTicket().getRegistry().getInMemory().getLoadFactor(),
                casProperties.getTicket().getRegistry().getInMemory().getConcurrency());
        r.setCipherExecutor(
                Beans.newTicketRegistryCipherExecutor(
                        casProperties.getTicket().getRegistry().getInMemory().getCrypto())
        );
        return r;
    }

    @Bean
    public TicketRegistrySupport defaultTicketRegistrySupport() {
        final DefaultTicketRegistrySupport s = new DefaultTicketRegistrySupport();
        s.setTicketRegistry(this.ticketRegistry);
        return s;
    }

    @Bean
    public UniqueTicketIdGenerator ticketGrantingTicketUniqueIdGenerator() {
        return new HostNameBasedUniqueTicketIdGenerator.TicketGrantingTicketIdGenerator(
                casProperties.getTicket().getTgt().getMaxLength(),
                casProperties.getHost().getName());
    }

    @Bean
    public UniqueTicketIdGenerator serviceTicketUniqueIdGenerator() {
        return new HostNameBasedUniqueTicketIdGenerator.ServiceTicketIdGenerator(
                casProperties.getTicket().getSt().getMaxLength(),
                casProperties.getHost().getName());
    }

    @Bean
    public UniqueTicketIdGenerator proxy20TicketUniqueIdGenerator() {
        return new HostNameBasedUniqueTicketIdGenerator.ProxyTicketIdGenerator(
                casProperties.getTicket().getPgt().getMaxLength(),
                casProperties.getHost().getName());
    }

    @Bean
    public ExpirationPolicy grantingTicketExpirationPolicy() {
        if (casProperties.getTicket().getTgt().getRememberMe().isEnabled()) {
            final RememberMeDelegatingExpirationPolicy p = new RememberMeDelegatingExpirationPolicy();
            p.setRememberMeExpirationPolicy(
                    new HardTimeoutExpirationPolicy(
                            casProperties.getTicket().getTgt().getRememberMe().getTimeToKillInSeconds(),
                            TimeUnit.SECONDS
                    )
            );
            p.setSessionExpirationPolicy(buildTicketGrantingTicketExpirationPolicy());
            return p;
        }
        
        return buildTicketGrantingTicketExpirationPolicy();
    }

    private ExpirationPolicy buildTicketGrantingTicketExpirationPolicy() {
        if (casProperties.getTicket().getTgt().getMaxTimeToLiveInSeconds() < 0
                && casProperties.getTicket().getTgt().getTimeToKillInSeconds() < 0) {
            LOGGER.warn("Ticket-granting ticket expiration policy is set to NEVER expire tickets.");
            return new NeverExpiresExpirationPolicy();
        }
        
        if (casProperties.getTicket().getTgt().getTimeout().getMaxTimeToLiveInSeconds() > 0) {
            final TimeoutExpirationPolicy t = new TimeoutExpirationPolicy(
                    casProperties.getTicket().getTgt().getTimeout().getMaxTimeToLiveInSeconds(),
                    TimeUnit.SECONDS
            );
            return t;
        }

        if (casProperties.getTicket().getTgt().getMaxTimeToLiveInSeconds() > 0
                && casProperties.getTicket().getTgt().getTimeToKillInSeconds() > 0) {
            final TicketGrantingTicketExpirationPolicy t = new TicketGrantingTicketExpirationPolicy(
                    casProperties.getTicket().getTgt().getMaxTimeToLiveInSeconds(),
                    casProperties.getTicket().getTgt().getTimeToKillInSeconds(),
                    TimeUnit.SECONDS
            );
            return t;
        }

        if (casProperties.getTicket().getTgt().getThrottledTimeout().getTimeInBetweenUsesInSeconds() > 0
                && casProperties.getTicket().getTgt().getThrottledTimeout().getTimeToKillInSeconds() > 0) {
            final ThrottledUseAndTimeoutExpirationPolicy p = new ThrottledUseAndTimeoutExpirationPolicy();
            p.setTimeToKillInMilliSeconds(TimeUnit.SECONDS.toMillis(
                    casProperties.getTicket().getTgt().getThrottledTimeout().getTimeToKillInSeconds()));
            p.setTimeInBetweenUsesInMilliSeconds(
                    TimeUnit.SECONDS.toMillis(
                            casProperties.getTicket().getTgt().getThrottledTimeout().getTimeInBetweenUsesInSeconds()));
            return p;
        }

        if (casProperties.getTicket().getTgt().getHardTimeout().getTimeToKillInSeconds() > 0) {
            final HardTimeoutExpirationPolicy h = new HardTimeoutExpirationPolicy(
                    casProperties.getTicket().getTgt().getHardTimeout().getTimeToKillInSeconds(),
                    TimeUnit.SECONDS
            );
            return h;
        }

        LOGGER.warn("Ticket-granting ticket expiration policy is set to ALWAYS expire tickets.");
        return new AlwaysExpiresExpirationPolicy();
    }

    @Bean
    public ExpirationPolicy serviceTicketExpirationPolicy() {
        return new MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy(
                casProperties.getTicket().getSt().getNumberOfUses(),
                TimeUnit.SECONDS.toMillis(casProperties.getTicket().getSt().getTimeToKillInSeconds()));

    }

    @Bean
    public ExpirationPolicy proxyTicketExpirationPolicy() {
        return new MultiTimeUseOrTimeoutExpirationPolicy.ProxyTicketExpirationPolicy(
                casProperties.getTicket().getPt().getNumberOfUses(),
                TimeUnit.SECONDS.toMillis(casProperties.getTicket().getPt().getTimeToKillInSeconds()));
    }

    @Bean
    public Map uniqueIdGeneratorsMap() {
        final Map<String, UniqueTicketIdGenerator> map = new HashMap<>();
        map.put("org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl", 
                serviceTicketUniqueIdGenerator());
        return map;
    }
    
    @ConditionalOnMissingBean
    @Bean
    public LockingStrategy lockingStrategy() {
        return new LockingStrategy() {
            @Override
            public boolean acquire() {
                return true;
            }

            /**
             * Does nothing.
             */
            @Override
            public void release() {
            }
        };
    }
     
    @ConditionalOnMissingBean
    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        final DefaultTicketRegistryCleaner c = new DefaultTicketRegistryCleaner();
        c.setLockingStrategy(lockingStrategy());
        c.setLogoutManager(logoutManager);
        c.setTicketRegistry(this.ticketRegistry);
        return c;
    }

    @ConditionalOnMissingBean
    @Bean
    public PlatformTransactionManager ticketTransactionManager() {
        return new PseudoTransactionManager();
    }
}
