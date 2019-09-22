package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.UniqueTicketIdGeneratorConfigurer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ServiceTicketIdGenerator;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasDefaultServiceTicketIdGeneratorsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casDefaultServiceTicketIdGeneratorsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasDefaultServiceTicketIdGeneratorsConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public UniqueTicketIdGenerator serviceTicketUniqueIdGenerator() {
        return new ServiceTicketIdGenerator(
            casProperties.getTicket().getSt().getMaxLength(),
            casProperties.getHost().getName());
    }

    @Bean
    public UniqueTicketIdGeneratorConfigurer casDefaultServiceTicketUniqueTicketIdGeneratorConfigurer() {
        return () -> CollectionUtils.wrap(Pair.of(SimpleWebApplicationServiceImpl.class.getName(), serviceTicketUniqueIdGenerator()));
    }
}
