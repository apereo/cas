package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.GoogleCloudStorageServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.google.cloud.storage.Storage;
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
 * This is {@link CasGoogleCloudStorageServiceRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistry, module = "gcp-storage")
@AutoConfiguration
public class CasGoogleCloudStorageServiceRegistryAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "googleCloudStorageServiceRegistry")
    public ServiceRegistry googleCloudStorageServiceRegistry(
        @Qualifier("storage")
        final Storage storage,
        @Qualifier(RegisteredServiceResourceNamingStrategy.BEAN_NAME)
        final RegisteredServiceResourceNamingStrategy registeredServiceResourceNamingStrategy,
        final ConfigurableApplicationContext applicationContext,
        final ObjectProvider<@NonNull List<ServiceRegistryListener>> serviceRegistryListeners) {
        return new GoogleCloudStorageServiceRegistry(applicationContext,
            Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new),
            registeredServiceResourceNamingStrategy, storage);
    }

    @Bean
    @ConditionalOnMissingBean(name = "googleCloudStorageServiceRegistryExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ServiceRegistryExecutionPlanConfigurer googleCloudStorageServiceRegistryExecutionPlanConfigurer(
        @Qualifier("googleCloudStorageServiceRegistry")
        final ServiceRegistry googleCloudStorageServiceRegistry) {
        return plan -> plan.registerServiceRegistry(googleCloudStorageServiceRegistry);
    }
}
