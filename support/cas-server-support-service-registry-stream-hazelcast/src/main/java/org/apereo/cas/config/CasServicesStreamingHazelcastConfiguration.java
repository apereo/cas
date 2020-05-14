package org.apereo.cas.config;

import org.apereo.cas.JmsQueueIdentifier;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.services.RegisteredServiceHazelcastDistributedCacheManager;
import org.apereo.cas.services.publisher.CasRegisteredServiceHazelcastStreamPublisher;
import org.apereo.cas.services.publisher.CasRegisteredServiceStreamPublisher;
import org.apereo.cas.services.replication.DefaultRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.util.cache.DistributedCacheManager;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
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
@ConditionalOnProperty(prefix = "cas.service-registry.stream", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class CasServicesStreamingHazelcastConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casRegisteredServiceStreamPublisherIdentifier")
    private ObjectProvider<JmsQueueIdentifier> casRegisteredServiceStreamPublisherIdentifier;

    @Bean
    public DistributedCacheManager registeredServiceDistributedCacheManager() {
        return new RegisteredServiceHazelcastDistributedCacheManager(casRegisteredServiceHazelcastInstance());
    }

    @Bean(destroyMethod = "destroy")
    public RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy() {
        val stream = casProperties.getServiceRegistry().getStream();
        return new DefaultRegisteredServiceReplicationStrategy(registeredServiceDistributedCacheManager(), stream);
    }

    @Bean
    public CasRegisteredServiceStreamPublisher casRegisteredServiceStreamPublisher() {
        return new CasRegisteredServiceHazelcastStreamPublisher(registeredServiceDistributedCacheManager(),
            casRegisteredServiceStreamPublisherIdentifier.getObject());
    }

    @Bean(destroyMethod = "shutdown")
    public HazelcastInstance casRegisteredServiceHazelcastInstance() {
        val name = CasRegisteredServiceHazelcastStreamPublisher.class.getSimpleName();
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
