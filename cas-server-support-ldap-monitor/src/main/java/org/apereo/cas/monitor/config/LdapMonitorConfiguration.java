package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.monitor.Monitor;
import org.apereo.cas.monitor.PooledLdapConnectionFactoryMonitor;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.SearchValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import java.util.concurrent.ExecutorService;

/**
 * This is {@link LdapMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ldapMonitorConfiguration")
public class LdapMonitorConfiguration {

    @Autowired
    @Qualifier("pooledConnectionFactoryMonitorConnectionFactory")
    private PooledConnectionFactory connectionFactory;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public ThreadPoolExecutorFactoryBean pooledConnectionFactoryMonitorExecutorService() {
        return Beans.newThreadPoolExecutorFactoryBean(casProperties.getMonitor().getLdap().getPool());
    }

    @Autowired
    @Bean
    public Monitor pooledLdapConnectionFactoryMonitor(
            @Qualifier("pooledConnectionFactoryMonitorExecutorService")
            final ExecutorService executor
    ) {
        final PooledLdapConnectionFactoryMonitor m =
                new PooledLdapConnectionFactoryMonitor(connectionFactory,
                        new SearchValidator());
        m.setMaxWait(casProperties.getMonitor().getMaxWait());
        m.setExecutor(executor);
        return m;
    }
}
