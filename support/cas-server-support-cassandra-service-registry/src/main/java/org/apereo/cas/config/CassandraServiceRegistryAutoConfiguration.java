package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.cassandra.DefaultCassandraSessionFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.cassandra.CassandraServiceRegistry;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link CassandraServiceRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistry, module = "cassandra")
@AutoConfiguration
public class CassandraServiceRegistryAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "cassandraServiceRegistry")
    public ServiceRegistry cassandraServiceRegistry(final CasConfigurationProperties casProperties,
                                                    final ConfigurableApplicationContext applicationContext,
                                                    final ObjectProvider<@NonNull List<ServiceRegistryListener>> serviceRegistryListeners,
                                                    @Qualifier("cassandraServiceRegistrySessionFactory")
                                                    final CassandraSessionFactory cassandraServiceRegistrySessionFactory) {
        val cassandra = casProperties.getServiceRegistry().getCassandra();
        return new CassandraServiceRegistry(cassandraServiceRegistrySessionFactory, cassandra, applicationContext,
            Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new));
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "cassandraServiceRegistrySessionFactory")
    public CassandraSessionFactory cassandraServiceRegistrySessionFactory(
        final CasConfigurationProperties casProperties,
        @Qualifier(CasSSLContext.BEAN_NAME)
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
