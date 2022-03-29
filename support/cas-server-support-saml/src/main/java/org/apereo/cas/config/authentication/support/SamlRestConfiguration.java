package org.apereo.cas.config.authentication.support;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.rest.factory.ServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.rest.plan.ServiceTicketResourceEntityResponseFactoryConfigurer;
import org.apereo.cas.support.saml.authentication.SamlRestServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SamlRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration(value = "SamlRestConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.Saml)
@ConditionalOnClass(value = ServiceTicketResourceEntityResponseFactoryConfigurer.class)
public class SamlRestConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "samlRestServiceTicketResourceEntityResponseFactory")
    public ServiceTicketResourceEntityResponseFactory samlRestServiceTicketResourceEntityResponseFactory(
        @Qualifier("samlServiceTicketUniqueIdGenerator")
        final UniqueTicketIdGenerator samlServiceTicketUniqueIdGenerator) {
        return new SamlRestServiceTicketResourceEntityResponseFactory(samlServiceTicketUniqueIdGenerator);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "samlRestServiceTicketResourceEntityResponseFactoryConfigurer")
    public ServiceTicketResourceEntityResponseFactoryConfigurer samlRestServiceTicketResourceEntityResponseFactoryConfigurer(
        @Qualifier("samlRestServiceTicketResourceEntityResponseFactory")
        final ServiceTicketResourceEntityResponseFactory samlRestServiceTicketResourceEntityResponseFactory) {
        return plan -> plan.registerFactory(samlRestServiceTicketResourceEntityResponseFactory);
    }

}
