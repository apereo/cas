package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.UniqueTicketIdGeneratorConfigurer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ServiceTicketIdGenerator;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasDefaultServiceTicketIdGeneratorsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry)
@Configuration(value = "CasDefaultServiceTicketIdGeneratorsConfiguration", proxyBeanMethods = false)
class CasDefaultServiceTicketIdGeneratorsConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public UniqueTicketIdGenerator serviceTicketUniqueIdGenerator(final CasConfigurationProperties casProperties) {
        return new ServiceTicketIdGenerator(
            casProperties.getTicket().getSt().getMaxLength(),
            casProperties.getHost().getName());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public UniqueTicketIdGeneratorConfigurer casDefaultServiceTicketUniqueTicketIdGeneratorConfigurer(
        @Qualifier("serviceTicketUniqueIdGenerator") final UniqueTicketIdGenerator serviceTicketUniqueIdGenerator) {
        return () -> CollectionUtils.wrap(
            Pair.of(SimpleWebApplicationServiceImpl.class.getName(), serviceTicketUniqueIdGenerator));
    }
}
