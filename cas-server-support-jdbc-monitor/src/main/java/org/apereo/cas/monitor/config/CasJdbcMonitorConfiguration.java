package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.DataSourceMonitor;
import org.apereo.cas.monitor.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;

/**
 * This is {@link CasJdbcMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casJdbcMonitorConfiguration")
public class CasJdbcMonitorConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Nullable
    @Autowired(required=false)
    @Qualifier("pooledConnectionFactoryMonitorExecutorService")
    private ExecutorService executor;
    
    @Autowired
    @Bean
    @RefreshScope
    public Monitor dataSourceMonitor(@Qualifier("monitorDataSource") final DataSource dataSource) {
        final DataSourceMonitor m = new DataSourceMonitor(dataSource);
        m.setValidationQuery(casProperties.getMonitor().getDataSource().getValidationQuery());
        m.setMaxWait(casProperties.getMonitor().getMaxWait());
        m.setExecutor(this.executor);
        return m;
    }
}
