package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.MonitorProperties;
import org.apereo.cas.monitor.PooledLdapConnectionFactoryHealthIndicator;
import org.apereo.cas.util.LdapUtils;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.SearchValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link LdapMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ldapMonitorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class LdapMonitorConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public HealthIndicator pooledLdapConnectionFactoryHealthIndicator() {
        final MonitorProperties.Ldap ldap = casProperties.getMonitor().getLdap();
        final PooledConnectionFactory connectionFactory = LdapUtils.newLdaptivePooledConnectionFactory(ldap);
        return new PooledLdapConnectionFactoryHealthIndicator((int) ldap.getMaxWait(),
            connectionFactory, new SearchValidator());
    }
}
