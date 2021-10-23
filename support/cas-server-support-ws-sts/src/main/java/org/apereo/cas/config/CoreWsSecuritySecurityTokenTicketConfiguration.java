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
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CoreWsSecuritySecurityTokenTicketConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration(value = "coreWsSecuritySecurityTokenTicketConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CoreWsSecuritySecurityTokenTicketConfiguration {
    
    @Configuration(value = "CoreWsSecuritySecurityTokenTicketFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CoreWsSecuritySecurityTokenTicketFactoryConfiguration {
        @ConditionalOnMissingBean(name = "securityTokenTicketFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public SecurityTokenTicketFactory securityTokenTicketFactory(
            @Qualifier("securityTokenTicketIdGenerator")
            final UniqueTicketIdGenerator securityTokenTicketIdGenerator,
            @Qualifier(ExpirationPolicyBuilder.BEAN_NAME_TICKET_GRANTING_TICKET_EXPIRATION_POLICY)
            final ExpirationPolicyBuilder grantingTicketExpirationPolicy) {
            return new DefaultSecurityTokenTicketFactory(securityTokenTicketIdGenerator, grantingTicketExpirationPolicy);
        }

        @ConditionalOnMissingBean(name = "securityTokenTicketIdGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public UniqueTicketIdGenerator securityTokenTicketIdGenerator() {
            return new DefaultUniqueTicketIdGenerator();
        }
        
    }
    
    @Configuration(value = "CoreWsSecuritySecurityTokenTicketPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CoreWsSecuritySecurityTokenTicketPlanConfiguration {
        @ConditionalOnMissingBean(name = "securityTokenTicketFactoryConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public TicketFactoryExecutionPlanConfigurer securityTokenTicketFactoryConfigurer(
            @Qualifier("securityTokenTicketFactory")
            final SecurityTokenTicketFactory securityTokenTicketFactory) {
            return () -> securityTokenTicketFactory;
        }
    }



}
