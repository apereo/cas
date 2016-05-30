package org.apereo.cas.monitor.config;

import org.apereo.cas.monitor.Monitor;
import org.apereo.cas.monitor.PooledConnectionFactoryMonitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link LdapMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ldapMonitorConfiguration")
public class LdapMonitorConfiguration {
    
    @Bean
    public Monitor pooledLdapConnectionFactoryMonitor() {
        return new PooledConnectionFactoryMonitor();
    }
}
