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

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link CasServicesStreamingHazelcastConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casServicesStreamingHazelcastConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.service-registry.stream", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class CasServicesStreamingHazelcastConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    public DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier> registeredServiceDistributedCacheManager() {
        val hz = casRegisteredServiceHazelcastInstance();
        val mapName = hz.getConfig().getMapConfigs().keySet().iterator().next();
        LOGGER.debug("Retrieving Hazelcast map [{}] for service replication", mapName);
        return new RegisteredServiceHazelcastDistributedCacheManager(hz, hz.getMap(mapName));
    }

    @Bean(destroyMethod = "shutdown")
    @RefreshScope
    @ConditionalOnMissingBean(name = "casRegisteredServiceHazelcastInstance")
    public HazelcastInstance casRegisteredServiceHazelcastInstance() {
        val name = DefaultCasRegisteredServiceStreamPublisher.class.getSimpleName();
        LOGGER.debug("Creating Hazelcast instance [{}] to publish service definitions", name);

        val stream = casProperties.getServiceRegistry().getStream().getHazelcast();
        val hzConfig = stream.getConfig();
        val duration = Beans.newDuration(stream.getDuration()).toMillis();
        val mapConfig = HazelcastConfigurationFactory.buildMapConfig(hzConfig, name, TimeUnit.MILLISECONDS.toSeconds(duration));

        val hazelcastInstance = Hazelcast.newHazelcastInstance(HazelcastConfigurationFactory.build(hzConfig));
        hazelcastInstance.getConfig().addMapConfig(mapConfig);
        return hazelcastInstance;
    }
}
