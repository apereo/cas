package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.monitor.Monitor;
import org.apereo.cas.monitor.PooledConnectionFactoryMonitor;
import org.ldaptive.Connection;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nullable;

/**
 * This is {@link LdapMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ldapMonitorConfiguration")
public class LdapMonitorConfiguration {

    /** Source of connections to validate. */
    @Nullable
    @Autowired(required=false)
    @Qualifier("pooledConnectionFactoryMonitorConnectionFactory")
    private PooledConnectionFactory connectionFactory;

    /** Connection validator. */
    @Nullable
    @Autowired(required=false)
    @Qualifier("pooledConnectionFactoryMonitorValidator")
    private Validator<Connection> validator;
    
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    public Monitor pooledLdapConnectionFactoryMonitor() {
        final PooledConnectionFactoryMonitor m = new PooledConnectionFactoryMonitor(connectionFactory, validator);
        m.setMaxWait(casProperties.getMonitor().getMaxWait());
        return m;
    }
}
