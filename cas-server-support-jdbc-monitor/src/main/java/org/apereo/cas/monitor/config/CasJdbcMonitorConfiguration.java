package org.apereo.cas.monitor.config;

import org.apereo.cas.monitor.DataSourceMonitor;
import org.apereo.cas.monitor.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * This is {@link CasJdbcMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casJdbcMonitorConfiguration")
public class CasJdbcMonitorConfiguration {
    
    @Autowired
    @Bean
    @RefreshScope
    public Monitor dataSourceMonitor(@Qualifier("monitorDataSource") final DataSource dataSource) {
        return new DataSourceMonitor(dataSource);
    }
}
