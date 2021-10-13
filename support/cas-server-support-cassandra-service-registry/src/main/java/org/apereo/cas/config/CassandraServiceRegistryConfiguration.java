package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.cassandra.DefaultCassandraSessionFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.cassandra.CassandraServiceRegistry;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link CassandraServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "cassandraServiceRegistryConfiguration", proxyBeanMethods = false)
public class CassandraServiceRegistryConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "cassandraServiceRegistry")
    @Autowired
    public ServiceRegistry cassandraServiceRegistry(final CasConfigurationProperties casProperties,
                                                    final ConfigurableApplicationContext applicationContext,
                                                    final ObjectProvider<List<ServiceRegistryListener>> serviceRegistryListeners,
                                                    @Qualifier("cassandraServiceRegistrySessionFactory")
                                                    final CassandraSessionFactory cassandraServiceRegistrySessionFactory) {
        val cassandra = casProperties.getServiceRegistry().getCassandra();
        return new CassandraServiceRegistry(cassandraServiceRegistrySessionFactory, cassandra, applicationContext,
            Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new));
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "cassandraServiceRegistrySessionFactory")
    @Autowired
    public CassandraSessionFactory cassandraServiceRegistrySessionFactory(
        final CasConfigurationProperties casProperties,
        @Qualifier("casSslContext")
        final CasSSLContext casSslContext) {
        val cassandra = casProperties.getServiceRegistry().getCassandra();
        return new DefaultCassandraSessionFactory(cassandra, casSslContext.getSslContext());
    }

    @Bean
    @ConditionalOnMissingBean(name = "cassandraServiceRegistryExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ServiceRegistryExecutionPlanConfigurer cassandraServiceRegistryExecutionPlanConfigurer(
        @Qualifier("cassandraServiceRegistry")
        final ServiceRegistry cassandraServiceRegistry) {
        return plan -> plan.registerServiceRegistry(cassandraServiceRegistry);
    }
}
