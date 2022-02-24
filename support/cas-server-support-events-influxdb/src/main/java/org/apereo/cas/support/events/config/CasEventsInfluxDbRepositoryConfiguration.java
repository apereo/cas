package org.apereo.cas.support.events.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.influxdb.InfluxDbConnectionFactory;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.dao.InfluxDbCasEventRepository;
import org.apereo.cas.util.spring.boot.ConditionalOnCasFeatureModule;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasEventsInfluxDbRepositoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "CasEventsInfluxDbRepositoryConfiguration", proxyBeanMethods = false)
@ConditionalOnCasFeatureModule(feature = CasFeatureModule.FeatureCatalog.Events, module = "influxDb")
public class CasEventsInfluxDbRepositoryConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "influxDbEventsConnectionFactory")
    public InfluxDbConnectionFactory influxDbEventsConnectionFactory(final CasConfigurationProperties casProperties) {
        return new InfluxDbConnectionFactory(casProperties.getEvents().getInfluxDb());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasEventRepository casEventRepository(
        @Qualifier("influxDbEventRepositoryFilter")
        final CasEventRepositoryFilter influxDbEventRepositoryFilter,
        @Qualifier("influxDbEventsConnectionFactory")
        final InfluxDbConnectionFactory influxDbEventsConnectionFactory) {
        return new InfluxDbCasEventRepository(influxDbEventRepositoryFilter, influxDbEventsConnectionFactory);
    }

    @ConditionalOnMissingBean(name = "influxDbEventRepositoryFilter")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasEventRepositoryFilter influxDbEventRepositoryFilter() {
        return CasEventRepositoryFilter.noOp();
    }
}
