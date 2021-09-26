package org.apereo.cas.support.events.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.influxdb.InfluxDbConnectionFactory;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.dao.InfluxDbCasEventRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasEventsInfluxDbRepositoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "casEventsInfluxDbRepositoryConfiguration", proxyBeanMethods = false)
public class CasEventsInfluxDbRepositoryConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "influxDbEventsConnectionFactory")
    @Autowired
    public InfluxDbConnectionFactory influxDbEventsConnectionFactory(final CasConfigurationProperties casProperties) {
        return new InfluxDbConnectionFactory(casProperties.getEvents().getInfluxDb());
    }

    @Bean
    public CasEventRepository casEventRepository(
        @Qualifier("influxDbEventRepositoryFilter")
        final CasEventRepositoryFilter influxDbEventRepositoryFilter,
        @Qualifier("influxDbEventsConnectionFactory")
        final InfluxDbConnectionFactory influxDbEventsConnectionFactory) {
        return new InfluxDbCasEventRepository(influxDbEventRepositoryFilter, influxDbEventsConnectionFactory);
    }

    @ConditionalOnMissingBean(name = "influxDbEventRepositoryFilter")
    @Bean
    public CasEventRepositoryFilter influxDbEventRepositoryFilter() {
        return CasEventRepositoryFilter.noOp();
    }
}
