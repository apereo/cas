package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.monitor.PooledLdapConnectionFactoryHealthIndicator;
import org.apereo.cas.util.LdapUtils;

import lombok.val;
import org.ldaptive.pool.SearchValidator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import java.util.concurrent.ExecutorService;

/**
 * This is {@link LdapMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "ldapMonitorConfiguration", proxyBeanMethods = true)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.monitor.ldap", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LdapMonitorConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public ThreadPoolExecutorFactoryBean pooledConnectionFactoryMonitorExecutorService() {
        return Beans.newThreadPoolExecutorFactoryBean(casProperties.getMonitor().getLdap().getPool());
    }

    @Autowired
    @Bean
    @ConditionalOnEnabledHealthIndicator("pooledLdapConnectionFactoryHealthIndicator")
    public HealthIndicator pooledLdapConnectionFactoryHealthIndicator(@Qualifier("pooledConnectionFactoryMonitorExecutorService")
                                                                      final ObjectProvider<ExecutorService> executor) {
        val ldap = casProperties.getMonitor().getLdap();
        val connectionFactory = LdapUtils.newLdaptivePooledConnectionFactory(ldap);
        return new PooledLdapConnectionFactoryHealthIndicator(Beans.newDuration(ldap.getMaxWait()).toMillis(),
            connectionFactory, executor.getObject(), new SearchValidator());
    }
}
