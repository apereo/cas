package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.monitor.JdbcDataSourceHealthIndicator;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;

/**
 * This is {@link CasJdbcMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "casJdbcMonitorConfiguration", proxyBeanMethods = false)
public class CasJdbcMonitorConfiguration {

    @Lazy
    @Bean
    @Autowired
    public ThreadPoolExecutorFactoryBean pooledJdbcMonitorExecutorService(final CasConfigurationProperties casProperties) {
        return Beans.newThreadPoolExecutorFactoryBean(casProperties.getMonitor().getJdbc().getPool());
    }

    @Autowired
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
    @Autowired
    public DataSource monitorDataSource(final CasConfigurationProperties casProperties) {
        return JpaBeans.newDataSource(casProperties.getMonitor().getJdbc());
    }
}
