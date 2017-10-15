package org.apereo.cas.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EntryListenerConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.hazelcast.BaseHazelcastProperties;
import org.apereo.cas.configuration.model.support.services.stream.hazelcast.StreamServicesHazelcastProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.listener.HazelcastRegisteredServiceEntryEventService;
import org.apereo.cas.services.listener.HazelcastRegisteredServiceEventListener;
import org.apereo.cas.services.listener.BaseThreadedRegisteredServiceEntryEventService;
import org.apereo.cas.services.publisher.CasRegisteredServiceHazelcastStreamPublisher;
import org.apereo.cas.services.publisher.CasRegisteredServiceStreamPublisher;
import org.apereo.cas.StringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link CasServicesStreamingHazelcastConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casServicesStreamingHazelcastConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasServicesStreamingHazelcastConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasServicesStreamingHazelcastConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;
    
    @Autowired
    @Qualifier("casRegisteredServiceStreamPublisherIdentifier")
    private StringBean casRegisteredServiceStreamPublisherIdentifier;

    @Autowired
    @Bean
    public CasRegisteredServiceStreamPublisher casRegisteredServiceStreamPublisher(@Qualifier("pooledRegisteredServiceHazelcastExecutorService") 
                                                                                   final ExecutorService executorService) {
        final String name = CasRegisteredServiceHazelcastStreamPublisher.class.getSimpleName();
        LOGGER.debug("Creating hazelcast instance [{}] to publish service definitions", name);

        final HazelcastConfigurationFactory factory = new HazelcastConfigurationFactory();
        final StreamServicesHazelcastProperties stream = casProperties.getServiceRegistry().getStream().getHazelcast();
        final BaseHazelcastProperties hz = stream.getConfig();
        final MapConfig mapConfig = factory.buildMapConfig(hz, name, TimeUnit.MILLISECONDS.toSeconds(stream.getDuration()));
        final Config cfg = factory.build(hz, mapConfig);

        final BaseThreadedRegisteredServiceEntryEventService service =
                new HazelcastRegisteredServiceEntryEventService(executorService, casRegisteredServiceStreamPublisherIdentifier, servicesManager);
        final EntryListener listener = new HazelcastRegisteredServiceEventListener(service);
        mapConfig.addEntryListenerConfig(new EntryListenerConfig(listener, true, true));
                
        final HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
        LOGGER.debug("Created hazelcast instance [{}] with publisher id [{}] to publish service definitions",
                name, casRegisteredServiceStreamPublisherIdentifier);
        return new CasRegisteredServiceHazelcastStreamPublisher(instance, casRegisteredServiceStreamPublisherIdentifier);
    }

    @Bean
    @Lazy
    public ThreadPoolExecutorFactoryBean pooledRegisteredServiceHazelcastExecutorService() {
        final StreamServicesHazelcastProperties stream = casProperties.getServiceRegistry().getStream().getHazelcast();
        return Beans.newThreadPoolExecutorFactoryBean(stream.getPool());
    }

}
