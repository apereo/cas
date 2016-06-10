package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link CasCoreTicketsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreTicketsConfiguration")
@EnableScheduling
@EnableAsync
public class CasCoreTicketsConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public ProxyGrantingTicketFactory defaultProxyGrantingTicketFactory() {
        return new DefaultProxyGrantingTicketFactory();
    }

    @RefreshScope
    @Bean
    public ProxyTicketFactory defaultProxyTicketFactory() {
        return new DefaultProxyTicketFactory();
    }

    @Bean
    public DefaultServiceTicketFactory defaultServiceTicketFactory() {
        return new DefaultServiceTicketFactory();
    }

    @Bean
    public DefaultTicketFactory defaultTicketFactory() {
        return new DefaultTicketFactory();
    }

    @Bean
    public DefaultTicketGrantingTicketFactory defaultTicketGrantingTicketFactory() {
        return new DefaultTicketGrantingTicketFactory();
    }

    @Bean
    public ProxyHandler proxy10Handler() {
        return new Cas10ProxyHandler();
    }

    @Bean
    public ProxyHandler proxy20Handler() {
        return new Cas20ProxyHandler();
    }

    @RefreshScope
    @Bean
    public TicketRegistry defaultTicketRegistry() {
        return new DefaultTicketRegistry(casProperties.getTicketRegistryProperties().getInMemory().getInitialCapacity(),
                casProperties.getTicketRegistryProperties().getInMemory().getLoadFactor(),
                casProperties.getTicketRegistryProperties().getInMemory().getConcurrency());
    }

    @Bean
    public TicketRegistrySupport defaultTicketRegistrySupport() {
        return new DefaultTicketRegistrySupport();
    }

    @Bean
    public UniqueTicketIdGenerator ticketGrantingTicketUniqueIdGenerator() {
        return new HostNameBasedUniqueTicketIdGenerator.TicketGrantingTicketIdGenerator(
                casProperties.getTicketGrantingTicketProperties().getMaxLength(),
                casProperties.getHostProperties().getName());
    }

    @Bean
    public UniqueTicketIdGenerator serviceTicketUniqueIdGenerator() {
        return new HostNameBasedUniqueTicketIdGenerator.ServiceTicketIdGenerator(
                casProperties.getServiceTicketProperties().getMaxLength(),
                casProperties.getHostProperties().getName());
    }

    @Bean
    public UniqueTicketIdGenerator proxy20TicketUniqueIdGenerator() {
        return new HostNameBasedUniqueTicketIdGenerator.ProxyTicketIdGenerator(
                casProperties.getProxyGrantingTicketProperties().getMaxLength(),
                casProperties.getHostProperties().getName());
    }

    @Bean
    public ExpirationPolicy timeoutExpirationPolicy() {
        final TimeoutExpirationPolicy t = new TimeoutExpirationPolicy(
                casProperties.getTicketGrantingTicketProperties().getTimeout().getMaxTimeToLiveInSeconds(),
                TimeUnit.SECONDS
        );
        return t;
    }

    @Bean
    public ExpirationPolicy ticketGrantingTicketExpirationPolicy() {
        final TicketGrantingTicketExpirationPolicy t = new TicketGrantingTicketExpirationPolicy(
                casProperties.getTicketGrantingTicketProperties().getMaxTimeToLiveInSeconds(),
                casProperties.getTicketGrantingTicketProperties().getTimeToKillInSeconds(),
                TimeUnit.SECONDS
        );
        return t;
    }

    @Bean
    public ExpirationPolicy throttledUseAndTimeoutExpirationPolicy() {
        final ThrottledUseAndTimeoutExpirationPolicy p = new ThrottledUseAndTimeoutExpirationPolicy();
        p.setTimeToKillInMilliSeconds(TimeUnit.SECONDS.toMillis(
                casProperties.getTicketGrantingTicketProperties().getThrottledTimeout().getTimeToKillInSeconds()));
        p.setTimeInBetweenUsesInMilliSeconds(
                TimeUnit.SECONDS.toMillis(
                        casProperties.getTicketGrantingTicketProperties().getThrottledTimeout().getTimeInBetweenUsesInSeconds()));
        return p;
    }

    @Bean
    public ExpirationPolicy rememberMeDelegatingExpirationPolicy() {
        return new RememberMeDelegatingExpirationPolicy();
    }

    @Bean
    public ExpirationPolicy neverExpiresExpirationPolicy() {
        return new NeverExpiresExpirationPolicy();
    }

    @Bean
    public ExpirationPolicy alwaysExpiresExpirationPolicy() {
        return new AlwaysExpiresExpirationPolicy();
    }

    @Bean
    public ExpirationPolicy hardTimeoutExpirationPolicy() {
        final HardTimeoutExpirationPolicy h = new HardTimeoutExpirationPolicy(
                casProperties.getTicketGrantingTicketProperties().getHardTimeout().getTimeToKillInSeconds(),
                TimeUnit.SECONDS
        );
        return h;
    }

    @Bean
    public ExpirationPolicy serviceTicketExpirationPolicy() {
        return new MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy(
                casProperties.getServiceTicketProperties().getNumberOfUses(),
                TimeUnit.SECONDS.toMillis(casProperties.getServiceTicketProperties().getTimeToKillInSeconds()));

    }

    @Bean
    public ExpirationPolicy proxyTicketExpirationPolicy() {
        return new MultiTimeUseOrTimeoutExpirationPolicy.ProxyTicketExpirationPolicy(
                casProperties.getProxyTicketProperties().getNumberOfUses(),
                TimeUnit.SECONDS.toMillis(casProperties.getProxyTicketProperties().getTimeToKillInSeconds()));
    }

    @ConditionalOnMissingBean(name = "lockingStrategy")
    @Bean
    public LockingStrategy lockingStrategy() {
        return new LockingStrategy() {
            @Override
            public boolean acquire() {
                return true;
            }

            @Override
            public void release() {
            }
        };
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return new DefaultTicketRegistryCleaner();
    }

    @Bean
    public PlatformTransactionManager ticketTransactionManager() {
        return new PseudoTransactionManager();
    }
}
