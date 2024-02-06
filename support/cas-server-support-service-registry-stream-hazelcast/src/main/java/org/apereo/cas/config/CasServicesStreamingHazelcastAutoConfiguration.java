package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceHazelcastDistributedCacheManager;
import org.apereo.cas.services.publisher.DefaultCasRegisteredServiceStreamPublisher;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link CasServicesStreamingHazelcastAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistryStreaming)
@AutoConfiguration
@Lazy(false)
public class CasServicesStreamingHazelcastAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.service-registry.stream.core.enabled").isTrue().evenIfMissing();

    @Configuration(value = "CasServicesStreamingHazelcastCacheConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasServicesStreamingHazelcastCacheConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier> registeredServiceDistributedCacheManager(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("casRegisteredServiceHazelcastInstance")
            final HazelcastInstance casRegisteredServiceHazelcastInstance) {
            return BeanSupplier.of(DistributedCacheManager.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val mapName = casRegisteredServiceHazelcastInstance.getConfig().getMapConfigs().keySet().iterator().next();
                    LOGGER.debug("Retrieving Hazelcast map [{}] for service replication", mapName);
                    return new RegisteredServiceHazelcastDistributedCacheManager(casRegisteredServiceHazelcastInstance,
                        casRegisteredServiceHazelcastInstance.getMap(mapName));
                })
                .otherwiseProxy()
                .get();
        }

    }

    @Configuration(value = "CasServicesStreamingHazelcastCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasServicesStreamingHazelcastCoreConfiguration {
        @Bean(destroyMethod = "shutdown")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "casRegisteredServiceHazelcastInstance")
        public HazelcastInstance casRegisteredServiceHazelcastInstance(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(HazelcastInstance.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val name = DefaultCasRegisteredServiceStreamPublisher.class.getSimpleName();
                    LOGGER.debug("Creating Hazelcast instance [{}] to publish service definitions", name);
                    val stream = casProperties.getServiceRegistry().getStream().getHazelcast();
                    val hzConfig = stream.getConfig();
                    val duration = Beans.newDuration(stream.getDuration()).toMillis();

                    val mapConfig = HazelcastConfigurationFactory.buildMapConfig(hzConfig, name, TimeUnit.MILLISECONDS.toSeconds(duration));
                    val config = HazelcastConfigurationFactory.build(hzConfig, mapConfig);
                    return HazelcastInstanceFactory.getOrCreateHazelcastInstance(config);
                })
                .otherwiseProxy()
                .get();
        }
    }
}
