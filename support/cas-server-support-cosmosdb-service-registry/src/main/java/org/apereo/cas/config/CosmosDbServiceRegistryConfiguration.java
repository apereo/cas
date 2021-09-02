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

import java.util.Collection;

/**
 * This is {@link CosmosDbServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "cosmosDbServiceRegistryConfiguration", proxyBeanMethods = true)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CosmosDbServiceRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casSslContext")
    private ObjectProvider<CasSSLContext> casSslContext;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @ConditionalOnMissingBean(name = "cosmosDbObjectFactory")
    @Bean
    public CosmosDbObjectFactory cosmosDbObjectFactory() {
        return new CosmosDbObjectFactory(casProperties.getServiceRegistry().getCosmosDb(), casSslContext.getObject());
    }

    @Bean
    @RefreshScope
    public ServiceRegistry cosmosDbServiceRegistry() {
        val cosmosDb = casProperties.getServiceRegistry().getCosmosDb();
        val factory = cosmosDbObjectFactory();
        factory.createDatabaseIfNecessary();
        if (cosmosDb.isCreateContainer()) {
            factory.createContainer(cosmosDb.getContainer(), CosmosDbServiceRegistry.PARTITION_KEY);
        }
        val container = factory.getContainer(cosmosDb.getContainer());
        return new CosmosDbServiceRegistry(container, applicationContext, serviceRegistryListeners.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "cosmosDbServiceRegistryExecutionPlanConfigurer")
    @RefreshScope
    @Autowired
    public ServiceRegistryExecutionPlanConfigurer cosmosDbServiceRegistryExecutionPlanConfigurer(
        @Qualifier("cosmosDbServiceRegistry") final ServiceRegistry cosmosDbServiceRegistry) {
        return plan -> plan.registerServiceRegistry(cosmosDbServiceRegistry);
    }
}
