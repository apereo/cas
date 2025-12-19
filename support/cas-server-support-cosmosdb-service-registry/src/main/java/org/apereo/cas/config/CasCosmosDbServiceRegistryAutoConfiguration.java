package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.cosmosdb.CosmosDbObjectFactory;
import org.apereo.cas.services.CosmosDbServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
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

/**
 * This is {@link CasCosmosDbServiceRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 * @deprecated Since 8.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistry, module = "cosmosdb")
@AutoConfiguration
@Deprecated(since = "8.0.0", forRemoval = true)
public class CasCosmosDbServiceRegistryAutoConfiguration {

    @ConditionalOnMissingBean(name = "cosmosDbObjectFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CosmosDbObjectFactory cosmosDbObjectFactory(
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext,
        final CasConfigurationProperties casProperties) throws Exception {
        return new CosmosDbObjectFactory(casProperties.getServiceRegistry().getCosmosDb(), casSslContext);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ServiceRegistry cosmosDbServiceRegistry(
        @Qualifier("cosmosDbObjectFactory")
        final CosmosDbObjectFactory cosmosDbObjectFactory,
        final ObjectProvider<@NonNull List<ServiceRegistryListener>> serviceRegistryListeners,
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
    public ServiceRegistryExecutionPlanConfigurer cosmosDbServiceRegistryExecutionPlanConfigurer(
        @Qualifier("cosmosDbServiceRegistry")
        final ServiceRegistry cosmosDbServiceRegistry) {
        return plan -> plan.registerServiceRegistry(cosmosDbServiceRegistry);
    }
}
