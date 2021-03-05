package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.CasServicesRegistryStreamingEventListener;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.publisher.CasRegisteredServiceStreamPublisher;
import org.apereo.cas.services.publisher.DefaultCasRegisteredServiceStreamPublisher;
import org.apereo.cas.services.replication.DefaultRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasServicesStreamingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casServicesStreamingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.service-registry.stream", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CasServicesStreamingConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public CasServicesRegistryStreamingEventListener casServicesRegistryStreamingEventListener() {
        return new CasServicesRegistryStreamingEventListener(casRegisteredServiceStreamPublisher(),
            casRegisteredServiceStreamPublisherIdentifier());
    }

    @RefreshScope
    @Bean(destroyMethod = "destroy")
    public RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy() {
        val stream = casProperties.getServiceRegistry().getStream();
        return new DefaultRegisteredServiceReplicationStrategy(registeredServiceDistributedCacheManager(), stream,
            casRegisteredServiceStreamPublisherIdentifier());
    }

    @Bean
    @RefreshScope
    public CasRegisteredServiceStreamPublisher casRegisteredServiceStreamPublisher() {
        return new DefaultCasRegisteredServiceStreamPublisher(registeredServiceDistributedCacheManager());
    }

    @Bean
    @ConditionalOnMissingBean(name = "registeredServiceDistributedCacheManager")
    public DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier> registeredServiceDistributedCacheManager() {
        return DistributedCacheManager.noOp();
    }

    @ConditionalOnMissingBean(name = "casRegisteredServiceStreamPublisherIdentifier")
    @Bean
    public PublisherIdentifier casRegisteredServiceStreamPublisherIdentifier() {
        return new PublisherIdentifier();
    }
}
