package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.monitor.JdbcDataSourceHealthIndicator;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;

/**
 * This is {@link CasJdbcMonitorAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Monitoring, module = "jdbc")
@AutoConfiguration
public class CasJdbcMonitorAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public FactoryBean<@NonNull ExecutorService> pooledJdbcMonitorExecutorService(final CasConfigurationProperties casProperties) {
        return Beans.newThreadPoolExecutorFactoryBean(casProperties.getMonitor().getJdbc().getPool());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnEnabledHealthIndicator("dataSourceHealthIndicator")
    public HealthIndicator dataSourceHealthIndicator(
        @Qualifier("pooledJdbcMonitorExecutorService")
        final ExecutorService executor, final CasConfigurationProperties casProperties,
        @Qualifier("monitorDataSource")
        final DataSource monitorDataSource) {
        val jdbc = casProperties.getMonitor().getJdbc();
        return new JdbcDataSourceHealthIndicator(Beans.newDuration(jdbc.getMaxWait()).toMillis(), monitorDataSource, executor, jdbc.getValidationQuery());
    }

    @ConditionalOnMissingBean(name = "monitorDataSource")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DataSource monitorDataSource(final CasConfigurationProperties casProperties) {
        return JpaBeans.newDataSource(casProperties.getMonitor().getJdbc());
    }
}
