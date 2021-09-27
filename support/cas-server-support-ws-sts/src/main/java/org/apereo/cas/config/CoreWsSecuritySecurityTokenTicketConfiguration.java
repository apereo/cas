package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.DefaultSecurityTokenTicketFactory;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.SecurityTokenTicketFactory;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CoreWsSecuritySecurityTokenTicketConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration(value = "coreWsSecuritySecurityTokenTicketConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CoreWsSecuritySecurityTokenTicketConfiguration {

    @ConditionalOnMissingBean(name = "securityTokenTicketFactory")
    @Bean
    @RefreshScope
    @Autowired
    public SecurityTokenTicketFactory securityTokenTicketFactory(
        @Qualifier("securityTokenTicketIdGenerator")
        final UniqueTicketIdGenerator securityTokenTicketIdGenerator,
        @Qualifier("grantingTicketExpirationPolicy")
        final ExpirationPolicyBuilder grantingTicketExpirationPolicy) {
        return new DefaultSecurityTokenTicketFactory(securityTokenTicketIdGenerator, grantingTicketExpirationPolicy);
    }

    @ConditionalOnMissingBean(name = "securityTokenTicketFactoryConfigurer")
    @Bean
    @RefreshScope
    @Autowired
    public TicketFactoryExecutionPlanConfigurer securityTokenTicketFactoryConfigurer(
        @Qualifier("securityTokenTicketFactory")
        final SecurityTokenTicketFactory securityTokenTicketFactory    ) {
        return () -> securityTokenTicketFactory;
    }

    @ConditionalOnMissingBean(name = "securityTokenTicketIdGenerator")
    @Bean
    @RefreshScope
    public UniqueTicketIdGenerator securityTokenTicketIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }

}
