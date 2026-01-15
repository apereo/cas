package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.CasServicesRegistryStreamingEventListener;
import org.apereo.cas.services.DefaultCasServicesRegistryStreamingEventListener;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.publisher.CasRegisteredServiceStreamPublisher;
import org.apereo.cas.services.publisher.DefaultCasRegisteredServiceStreamPublisher;
import org.apereo.cas.services.replication.DefaultRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasServicesStreamingAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistryStreaming)
@AutoConfiguration
public class CasServicesStreamingAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.service-registry.stream.core.enabled").isTrue().evenIfMissing();

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Lazy(false)
    public CasServicesRegistryStreamingEventListener casServicesRegistryStreamingEventListener(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("casRegisteredServiceStreamPublisher")
        final CasRegisteredServiceStreamPublisher casRegisteredServiceStreamPublisher,
        @Qualifier("casRegisteredServiceStreamPublisherIdentifier")
        final PublisherIdentifier casRegisteredServiceStreamPublisherIdentifier) {
        return BeanSupplier.of(CasServicesRegistryStreamingEventListener.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new DefaultCasServicesRegistryStreamingEventListener(casRegisteredServiceStreamPublisher,
                casRegisteredServiceStreamPublisherIdentifier))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("registeredServiceDistributedCacheManager")
        final DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier> registeredServiceDistributedCacheManager,
        @Qualifier("casRegisteredServiceStreamPublisherIdentifier")
        final PublisherIdentifier casRegisteredServiceStreamPublisherIdentifier) {
        return BeanSupplier.of(RegisteredServiceReplicationStrategy.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val stream = casProperties.getServiceRegistry().getStream();
                return new DefaultRegisteredServiceReplicationStrategy(registeredServiceDistributedCacheManager,
                    stream, casRegisteredServiceStreamPublisherIdentifier);
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasRegisteredServiceStreamPublisher casRegisteredServiceStreamPublisher(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("registeredServiceDistributedCacheManager")
        final DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier> registeredServiceDistributedCacheManager) {
        return BeanSupplier.of(CasRegisteredServiceStreamPublisher.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new DefaultCasRegisteredServiceStreamPublisher(registeredServiceDistributedCacheManager))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "registeredServiceDistributedCacheManager")
    public DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier> registeredServiceDistributedCacheManager() {
        return DistributedCacheManager.noOp();
    }

    @ConditionalOnMissingBean(name = "casRegisteredServiceStreamPublisherIdentifier")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PublisherIdentifier casRegisteredServiceStreamPublisherIdentifier() {
        return new PublisherIdentifier();
    }
}
