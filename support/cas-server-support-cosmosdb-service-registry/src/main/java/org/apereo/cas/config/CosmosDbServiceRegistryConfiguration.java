package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.cosmosdb.CosmosDbObjectFactory;
import org.apereo.cas.services.CosmosDbServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;

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
 * This is {@link CosmosDbServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "cosmosDbServiceRegistryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CosmosDbServiceRegistryConfiguration {

    @ConditionalOnMissingBean(name = "cosmosDbObjectFactory")
    @Bean
    @Autowired
    public CosmosDbObjectFactory cosmosDbObjectFactory(
        @Qualifier("casSslContext")
        final CasSSLContext casSslContext,
        final CasConfigurationProperties casProperties) {
        return new CosmosDbObjectFactory(casProperties.getServiceRegistry().getCosmosDb(), casSslContext);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public ServiceRegistry cosmosDbServiceRegistry(
        @Qualifier("cosmosDbObjectFactory")
        final CosmosDbObjectFactory cosmosDbObjectFactory,
        final ObjectProvider<List<ServiceRegistryListener>> serviceRegistryListeners,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val cosmosDb = casProperties.getServiceRegistry().getCosmosDb();
        cosmosDbObjectFactory.createDatabaseIfNecessary();
        if (cosmosDb.isCreateContainer()) {
            cosmosDbObjectFactory.createContainer(cosmosDb.getContainer(), CosmosDbServiceRegistry.PARTITION_KEY);
        }
        val container = cosmosDbObjectFactory.getContainer(cosmosDb.getContainer());
        return new CosmosDbServiceRegistry(container, applicationContext,
            Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new));
    }

    @Bean
    @ConditionalOnMissingBean(name = "cosmosDbServiceRegistryExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public ServiceRegistryExecutionPlanConfigurer cosmosDbServiceRegistryExecutionPlanConfigurer(
        @Qualifier("cosmosDbServiceRegistry")
        final ServiceRegistry cosmosDbServiceRegistry) {
        return plan -> plan.registerServiceRegistry(cosmosDbServiceRegistry);
    }
}
