package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceHazelcastDistributedCacheManager;
import org.apereo.cas.services.publisher.DefaultCasRegisteredServiceStreamPublisher;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link CasServicesStreamingHazelcastConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.service-registry.stream", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
@Configuration(value = "casServicesStreamingHazelcastConfiguration", proxyBeanMethods = false)
public class CasServicesStreamingHazelcastConfiguration {

    @Configuration(value = "CasServicesStreamingHazelcastCacheConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasServicesStreamingHazelcastCacheConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier> registeredServiceDistributedCacheManager(
            @Qualifier("casRegisteredServiceHazelcastInstance")
            final HazelcastInstance casRegisteredServiceHazelcastInstance) {
            val mapName = casRegisteredServiceHazelcastInstance.getConfig().getMapConfigs().keySet().iterator().next();
            LOGGER.debug("Retrieving Hazelcast map [{}] for service replication", mapName);
            return new RegisteredServiceHazelcastDistributedCacheManager(casRegisteredServiceHazelcastInstance,
                casRegisteredServiceHazelcastInstance.getMap(mapName));
        }

    }

    @Configuration(value = "CasServicesStreamingHazelcastCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasServicesStreamingHazelcastCoreConfiguration {
        @Bean(destroyMethod = "shutdown")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "casRegisteredServiceHazelcastInstance")
        @Autowired
        public HazelcastInstance casRegisteredServiceHazelcastInstance(final CasConfigurationProperties casProperties) {
            val name = DefaultCasRegisteredServiceStreamPublisher.class.getSimpleName();
            LOGGER.debug("Creating Hazelcast instance [{}] to publish service definitions", name);
            val stream = casProperties.getServiceRegistry().getStream().getHazelcast();
            val hzConfig = stream.getConfig();
            val duration = Beans.newDuration(stream.getDuration()).toMillis();

            val mapConfig = HazelcastConfigurationFactory.buildMapConfig(hzConfig, name, TimeUnit.MILLISECONDS.toSeconds(duration));
            val config = HazelcastConfigurationFactory.build(hzConfig, mapConfig);
            return HazelcastInstanceFactory.getOrCreateHazelcastInstance(config);
        }
    }
}
