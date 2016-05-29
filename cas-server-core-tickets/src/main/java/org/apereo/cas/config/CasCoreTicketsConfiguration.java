package org.apereo.cas.config;

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
import org.apereo.cas.ticket.registry.DefaultTicketRegistrySupport;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.support.ThrottledUseAndTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.support.TimeoutExpirationPolicy;
import org.apereo.cas.util.HostNameBasedUniqueTicketIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreTicketsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreTicketsConfiguration")
public class CasCoreTicketsConfiguration {

    @Value("${st.numberOfUses:1}")
    private int numberOfUses;
    
    @Value("#{${st.timeToKillInSeconds:10}*1000}")
    private long timeToKillInMilliSeconds;

    @Value("${pt.numberOfUses:1}")
    private int numberOfUsesPt;
    
    @Value("#{${pt.timeToKillInSeconds:10}*1000}")
    private long timeToKillInMilliSecondsPt;
            
            
    @Value("${tgt.ticket.maxlength:50}")
    private int maxLengthTgt;

    @Value("${pgt.ticket.maxlength:50}")
    private int maxLengthPgt;
    
    @Value("${host.name:cas01.example.org}")
    private String suffix;

    @Value("${st.ticket.maxlength:20}")
    private int maxLengthSt;

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
        return new DefaultTicketRegistry();
    }
    
    @Bean
    public TicketRegistrySupport defaultTicketRegistrySupport() {
        return new DefaultTicketRegistrySupport();
    }
    
    @Bean
    public UniqueTicketIdGenerator ticketGrantingTicketUniqueIdGenerator() {
        return new HostNameBasedUniqueTicketIdGenerator.TicketGrantingTicketIdGenerator(this.maxLengthTgt, this.suffix);
    }

    @Bean
    public UniqueTicketIdGenerator serviceTicketUniqueIdGenerator() {
        return new HostNameBasedUniqueTicketIdGenerator.ServiceTicketIdGenerator(this.maxLengthSt, this.suffix);
    }

    @Bean
    public UniqueTicketIdGenerator proxy20TicketUniqueIdGenerator() {
        return new HostNameBasedUniqueTicketIdGenerator.ProxyTicketIdGenerator(this.maxLengthPgt, this.suffix);
    }

    @Bean
    @RefreshScope
    public ExpirationPolicy timeoutExpirationPolicy() {
        return new TimeoutExpirationPolicy();
    }

    @Bean
    @RefreshScope
    public ExpirationPolicy ticketGrantingTicketExpirationPolicy() {
        return new TicketGrantingTicketExpirationPolicy();
    }

    @Bean
    @RefreshScope
    public ExpirationPolicy throttledUseAndTimeoutExpirationPolicy() {
        return new ThrottledUseAndTimeoutExpirationPolicy();
    }

    @Bean
    @RefreshScope
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
    @RefreshScope
    public ExpirationPolicy hardTimeoutExpirationPolicy() {
        return new HardTimeoutExpirationPolicy();
    }

    @Bean
    @RefreshScope
    public ExpirationPolicy serviceTicketExpirationPolicy() {
        return new MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy(this.numberOfUses, 
                                                                                       this.timeToKillInMilliSeconds);
    }

    @Bean
    @RefreshScope
    public ExpirationPolicy proxyTicketExpirationPolicy() {
        return new MultiTimeUseOrTimeoutExpirationPolicy.ProxyTicketExpirationPolicy(this.numberOfUsesPt, 
                                                                                     this.timeToKillInMilliSecondsPt);
    }
    
}
