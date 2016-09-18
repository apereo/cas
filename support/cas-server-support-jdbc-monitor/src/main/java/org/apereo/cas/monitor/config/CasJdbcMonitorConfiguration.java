package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.monitor.JdbcDataSourceMonitor;
import org.apereo.cas.monitor.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;

/**
 * This is {@link CasJdbcMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casJdbcMonitorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasJdbcMonitorConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Bean
    @RefreshScope
    public Monitor dataSourceMonitor(
            @Qualifier("pooledJdbcMonitorExecutorService")
            final ExecutorService executor) {
        final JdbcDataSourceMonitor m = new JdbcDataSourceMonitor(monitorDataSource());
        m.setValidationQuery(casProperties.getMonitor().getJdbc().getValidationQuery());
        m.setMaxWait(casProperties.getMonitor().getJdbc().getMaxWait());
        m.setExecutor(executor);
        return m;
    }

    @Lazy
    @Bean
    public ThreadPoolExecutorFactoryBean pooledJdbcMonitorExecutorService() {
        return Beans.newThreadPoolExecutorFactoryBean(casProperties.getMonitor().getJdbc().getPool());
    }

    @RefreshScope
    @Bean
    public DataSource monitorDataSource() {
        return Beans.newHickariDataSource(casProperties.getMonitor().getJdbc());
    }
}
