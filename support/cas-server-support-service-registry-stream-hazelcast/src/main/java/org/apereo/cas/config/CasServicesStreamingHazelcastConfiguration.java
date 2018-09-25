package org.apereo.cas.config;

import org.apereo.cas.DistributedCacheManager;
import org.apereo.cas.StringBean;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.hazelcast.BaseHazelcastProperties;
import org.apereo.cas.configuration.model.support.services.stream.StreamingServiceRegistryProperties;
import org.apereo.cas.configuration.model.support.services.stream.hazelcast.StreamServicesHazelcastProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.services.RegisteredServiceHazelcastDistributedCacheManager;
import org.apereo.cas.services.publisher.CasRegisteredServiceHazelcastStreamPublisher;
import org.apereo.cas.services.publisher.CasRegisteredServiceStreamPublisher;
import org.apereo.cas.services.replication.DefaultRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@ConditionalOnProperty(prefix = "cas.serviceRegistry.stream", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class CasServicesStreamingHazelcastConfiguration {


    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    @Qualifier("casRegisteredServiceStreamPublisherIdentifier")
    private StringBean casRegisteredServiceStreamPublisherIdentifier;

    @Autowired
    @Qualifier("casHazelcastInstance")
    private HazelcastInstance hazelcastInstance;

    @Bean
    public DistributedCacheManager registeredServiceDistributedCacheManager() {
        return new RegisteredServiceHazelcastDistributedCacheManager(casRegisteredServiceHazelcastInstance());
    }

    @Bean
    public RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy() {
        final StreamingServiceRegistryProperties stream = casProperties.getServiceRegistry().getStream();
        return new DefaultRegisteredServiceReplicationStrategy(registeredServiceDistributedCacheManager(), stream);
    }
    
    @Bean
    public CasRegisteredServiceStreamPublisher casRegisteredServiceStreamPublisher() {
        return new CasRegisteredServiceHazelcastStreamPublisher(registeredServiceDistributedCacheManager(),
                casRegisteredServiceStreamPublisherIdentifier);
    }

    @Bean
    public HazelcastInstance casRegisteredServiceHazelcastInstance() {
        final String name = CasRegisteredServiceHazelcastStreamPublisher.class.getSimpleName();
        LOGGER.debug("Creating Hazelcast instance [{}] to publish service definitions", name);
        final HazelcastConfigurationFactory factory = new HazelcastConfigurationFactory();
        final StreamServicesHazelcastProperties stream = casProperties.getServiceRegistry().getStream().getHazelcast();
        final BaseHazelcastProperties hz = stream.getConfig();
        final long duration = Beans.newDuration(stream.getDuration()).toMillis();
        final MapConfig mapConfig = factory.buildMapConfig(hz, name,
            TimeUnit.MILLISECONDS.toSeconds(duration));
        hazelcastInstance.getConfig().addMapConfig(mapConfig);
        return hazelcastInstance;
    }
}
