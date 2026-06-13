package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.CasGoogleCloudServiceRegistryMessageReceiver;
import org.apereo.cas.services.CasGoogleCloudStorageServiceRegistryListener;
import org.apereo.cas.services.CasGoogleCloudStorageSubscriptionCustomizer;
import org.apereo.cas.services.GoogleCloudStorageServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.storage.Storage;
import com.google.pubsub.v1.ProjectSubscriptionName;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        final ObjectProvider<List<ServiceRegistryListener>> serviceRegistryListeners) {
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

    @Configuration(value = "CasGoogleCloudStorageServiceRegistryPubSubAutoConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistry, module = "gcp-storage-pubsub", enabledByDefault = false)
    static class CasGoogleCloudStorageServiceRegistryPubSubAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "googleCloudStorageServiceRegistryListener")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasGoogleCloudStorageServiceRegistryListener googleCloudStorageServiceRegistryListener(
            @Qualifier(RegisteredServiceResourceNamingStrategy.BEAN_NAME)
            final RegisteredServiceResourceNamingStrategy namingStrategy,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("storage") final Storage storage,
            @Qualifier("googleCloudStorageServiceRegistry")
            final ServiceRegistry googleCloudStorageServiceRegistry,
            @Qualifier("googleCloudStorageSubscriptionCustomizer")
            final CasGoogleCloudStorageSubscriptionCustomizer googleCloudStorageSubscriptionCustomizer) {
            val subscription = ProjectSubscriptionName.of(storage.getOptions().getProjectId(),
                CasGoogleCloudStorageServiceRegistryListener.SUBSCRIPTION_NAME);
            val serializer = new RegisteredServiceJsonSerializer(applicationContext);
            val receiver = new CasGoogleCloudServiceRegistryMessageReceiver(googleCloudStorageServiceRegistry, storage, serializer, namingStrategy);
            val subscriberBuilder = googleCloudStorageSubscriptionCustomizer.customize(Subscriber.newBuilder(subscription, receiver));
            val subscriber = subscriberBuilder.build();
            return new CasGoogleCloudStorageServiceRegistryListener(subscriber);
        }

        @Bean
        @ConditionalOnMissingBean(name = "googleCloudStorageSubscriptionCustomizer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasGoogleCloudStorageSubscriptionCustomizer googleCloudStorageSubscriptionCustomizer() {
            return CasGoogleCloudStorageSubscriptionCustomizer.noOp();
        }
    }

}
