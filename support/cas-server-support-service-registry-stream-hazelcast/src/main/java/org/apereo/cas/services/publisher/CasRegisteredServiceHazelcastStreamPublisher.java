package org.apereo.cas.services.publisher;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.service.BaseCasRegisteredServiceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * This is {@link CasRegisteredServiceHazelcastStreamPublisher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasRegisteredServiceHazelcastStreamPublisher implements CasRegisteredServiceStreamPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasRegisteredServiceHazelcastStreamPublisher.class);
    
    private final HazelcastInstance instance;
    private final String mapName;
    private final PublisherIdentifier publisherId;

    public CasRegisteredServiceHazelcastStreamPublisher(final HazelcastInstance instance,
                                                        final PublisherIdentifier publisherId) {
        this.instance = instance;
        mapName = instance.getConfig().getMapConfigs().keySet().iterator().next();
        this.publisherId = publisherId;
    }

    @Override
    public void publish(final RegisteredService service, final ApplicationEvent event) {
        if (!BaseCasRegisteredServiceEvent.class.isAssignableFrom(event.getClass())) {
            return;   
        }
        
        final IMap<String, RegisteredServicesQueuedEvent> inst = instance.getMap(mapName);
        LOGGER.debug("Publishing service definition [{}] into Hazelcast queue [{}] for event [{}] with publisher [{}]", 
               service.getName(), mapName, event.getClass().getSimpleName(), this.publisherId);
        inst.set(service.getName(), new RegisteredServicesQueuedEvent(LocalDateTime.now().toString(), event, service, this.publisherId));
    }

}
