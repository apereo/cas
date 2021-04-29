package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.DefaultSecurityTokenTicketFactory;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.SecurityTokenTicketFactory;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import org.springframework.beans.factory.ObjectProvider;
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
@Configuration("coreWsSecuritySecurityTokenTicketConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CoreWsSecuritySecurityTokenTicketConfiguration {

    @Autowired
    @Qualifier("grantingTicketExpirationPolicy")
    private ObjectProvider<ExpirationPolicyBuilder> grantingTicketExpirationPolicy;

    @ConditionalOnMissingBean(name = "securityTokenTicketFactory")
    @Bean
    @RefreshScope
    public SecurityTokenTicketFactory securityTokenTicketFactory() {
        return new DefaultSecurityTokenTicketFactory(securityTokenTicketIdGenerator(), grantingTicketExpirationPolicy.getObject());
    }

    @ConditionalOnMissingBean(name = "securityTokenTicketFactoryConfigurer")
    @Bean
    @RefreshScope
    public TicketFactoryExecutionPlanConfigurer securityTokenTicketFactoryConfigurer() {
        return this::securityTokenTicketFactory;
    }

    @ConditionalOnMissingBean(name = "securityTokenTicketIdGenerator")
    @Bean
    @RefreshScope
    public UniqueTicketIdGenerator securityTokenTicketIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }

}
