package org.apereo.cas.config;

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

import javax.net.ssl.SSLContext;
import java.util.Collection;

/**
 * This is {@link CassandraServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("cassandraServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CassandraServiceRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @Autowired
    @Qualifier("sslContext")
    private ObjectProvider<SSLContext> sslContext;

    @Bean
    @RefreshScope
    public ServiceRegistry cassandraServiceRegistry() {
        val cassandra = casProperties.getServiceRegistry().getCassandra();
        return new CassandraServiceRegistry(cassandraServiceRegistrySessionFactory(), cassandra,
            applicationContext, serviceRegistryListeners.getObject());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "cassandraServiceRegistrySessionFactory")
    public CassandraSessionFactory cassandraServiceRegistrySessionFactory() {
        val cassandra = casProperties.getServiceRegistry().getCassandra();
        return new DefaultCassandraSessionFactory(cassandra, sslContext.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "cassandraServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer cassandraServiceRegistryExecutionPlanConfigurer() {
        return plan -> plan.registerServiceRegistry(cassandraServiceRegistry());
    }
}
