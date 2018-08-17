package org.apereo.cas.config.authentication.support;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.factory.ServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.plan.ServiceTicketResourceEntityResponseFactoryConfigurer;
import org.apereo.cas.rest.plan.ServiceTicketResourceEntityResponseFactoryPlan;
import org.apereo.cas.support.saml.authentication.SamlRestServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("samlRestConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnClass(ServiceTicketResourceEntityResponseFactoryConfigurer.class)
public class SamlRestConfiguration implements ServiceTicketResourceEntityResponseFactoryConfigurer {

    @Autowired
    @Qualifier("samlServiceTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator samlServiceTicketUniqueIdGenerator;

    @Bean
    public ServiceTicketResourceEntityResponseFactory samlRestServiceTicketResourceEntityResponseFactory() {
        return new SamlRestServiceTicketResourceEntityResponseFactory(samlServiceTicketUniqueIdGenerator);
    }

    @Override
    public void configureEntityResponseFactory(final ServiceTicketResourceEntityResponseFactoryPlan plan) {
        plan.registerFactory(samlRestServiceTicketResourceEntityResponseFactory());
    }
}
