package org.apereo.cas.config.authentication.support;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.factory.ServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.plan.ServiceTicketResourceEntityResponseFactoryConfigurer;
import org.apereo.cas.support.saml.authentication.SamlRestServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration(value = "samlRestConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnClass(value = ServiceTicketResourceEntityResponseFactoryConfigurer.class)
public class SamlRestConfiguration {

    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "samlRestServiceTicketResourceEntityResponseFactory")
    public ServiceTicketResourceEntityResponseFactory samlRestServiceTicketResourceEntityResponseFactory(
        @Qualifier("samlServiceTicketUniqueIdGenerator")
        final UniqueTicketIdGenerator samlServiceTicketUniqueIdGenerator) {
        return new SamlRestServiceTicketResourceEntityResponseFactory(samlServiceTicketUniqueIdGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(name = "samlRestServiceTicketResourceEntityResponseFactoryConfigurer")
    @Autowired
    public ServiceTicketResourceEntityResponseFactoryConfigurer samlRestServiceTicketResourceEntityResponseFactoryConfigurer(
        @Qualifier("samlRestServiceTicketResourceEntityResponseFactory")
        final ServiceTicketResourceEntityResponseFactory samlRestServiceTicketResourceEntityResponseFactory) {
        return plan -> plan.registerFactory(samlRestServiceTicketResourceEntityResponseFactory);
    }

}
