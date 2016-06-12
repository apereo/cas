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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Map;
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

    @Autowired
    @Qualifier("ticketGrantingTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    @Autowired
    @Qualifier("grantingTicketExpirationPolicy")
    private ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    @Nullable
    @Autowired(required = false)
    @Qualifier("rememberMeExpirationPolicy")
    private ExpirationPolicy rememberMeExpirationPolicy;

    @Nullable
    @Autowired(required = false)
    @Qualifier("sessionExpirationPolicy")
    private ExpirationPolicy sessionExpirationPolicy;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Resource(name = "uniqueIdGeneratorsMap")
    protected Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService;

    @Bean
    public ProxyGrantingTicketFactory defaultProxyGrantingTicketFactory() {
        final DefaultProxyGrantingTicketFactory f = new DefaultProxyGrantingTicketFactory();
        f.setTicketGrantingTicketExpirationPolicy(ticketGrantingTicketExpirationPolicy);
        f.setTicketGrantingTicketUniqueTicketIdGenerator(ticketGrantingTicketUniqueTicketIdGenerator);
        return f;
    }

    @RefreshScope
    @Bean
    public ProxyTicketFactory defaultProxyTicketFactory() {
        final DefaultProxyTicketFactory f = new DefaultProxyTicketFactory();
        f.setProxyTicketExpirationPolicy(proxyTicketExpirationPolicy());
        f.setUniqueTicketIdGeneratorsForService(uniqueTicketIdGeneratorsForService);
        return f;
    }

    @Bean
    public DefaultServiceTicketFactory defaultServiceTicketFactory() {
        final DefaultServiceTicketFactory f = new DefaultServiceTicketFactory();
        f.setServiceTicketExpirationPolicy(serviceTicketExpirationPolicy());
        f.setUniqueTicketIdGeneratorsForService(uniqueTicketIdGeneratorsForService);
        return f;
    }

    @Bean
    public DefaultTicketFactory defaultTicketFactory() {
        return new DefaultTicketFactory();
    }

    @Bean
    public DefaultTicketGrantingTicketFactory defaultTicketGrantingTicketFactory() {
        final DefaultTicketGrantingTicketFactory f = new DefaultTicketGrantingTicketFactory();
        f.setTicketGrantingTicketExpirationPolicy(ticketGrantingTicketExpirationPolicy);
        f.setTicketGrantingTicketUniqueTicketIdGenerator(ticketGrantingTicketUniqueTicketIdGenerator);
        return f;
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
        return new DefaultTicketRegistry(
                casProperties.getTicket().getRegistry().getInMemory().getInitialCapacity(),
                casProperties.getTicket().getRegistry().getInMemory().getLoadFactor(),
                casProperties.getTicket().getRegistry().getInMemory().getConcurrency());
    }

    @Bean
    public TicketRegistrySupport defaultTicketRegistrySupport() {
        final DefaultTicketRegistrySupport s = new DefaultTicketRegistrySupport();
        s.setTicketRegistry(ticketRegistry);
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
    public ExpirationPolicy timeoutExpirationPolicy() {
        final TimeoutExpirationPolicy t = new TimeoutExpirationPolicy(
                casProperties.getTicket().getTgt().getTimeout().getMaxTimeToLiveInSeconds(),
                TimeUnit.SECONDS
        );
        return t;
    }

    @Bean
    public ExpirationPolicy ticketGrantingTicketExpirationPolicy() {
        final TicketGrantingTicketExpirationPolicy t = new TicketGrantingTicketExpirationPolicy(
                casProperties.getTicket().getTgt().getMaxTimeToLiveInSeconds(),
                casProperties.getTicket().getTgt().getTimeToKillInSeconds(),
                TimeUnit.SECONDS
        );
        return t;
    }

    @Bean
    public ExpirationPolicy throttledUseAndTimeoutExpirationPolicy() {
        final ThrottledUseAndTimeoutExpirationPolicy p = new ThrottledUseAndTimeoutExpirationPolicy();
        p.setTimeToKillInMilliSeconds(TimeUnit.SECONDS.toMillis(
                casProperties.getTicket().getTgt().getThrottledTimeout().getTimeToKillInSeconds()));
        p.setTimeInBetweenUsesInMilliSeconds(
                TimeUnit.SECONDS.toMillis(
                        casProperties.getTicket().getTgt().getThrottledTimeout().getTimeInBetweenUsesInSeconds()));
        return p;
    }

    @Bean
    public ExpirationPolicy rememberMeDelegatingExpirationPolicy() {
        final RememberMeDelegatingExpirationPolicy p = new RememberMeDelegatingExpirationPolicy();
        p.setRememberMeExpirationPolicy(rememberMeExpirationPolicy);
        p.setSessionExpirationPolicy(sessionExpirationPolicy);
        return p;
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
                casProperties.getTicket().getTgt().getHardTimeout().getTimeToKillInSeconds(),
                TimeUnit.SECONDS
        );
        return h;
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
