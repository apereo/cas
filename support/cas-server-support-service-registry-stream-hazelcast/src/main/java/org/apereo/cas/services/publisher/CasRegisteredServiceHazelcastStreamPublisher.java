package org.apereo.cas.services.publisher;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apereo.cas.StringBean;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

/**
 * This is {@link CasRegisteredServiceHazelcastStreamPublisher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasRegisteredServiceHazelcastStreamPublisher extends BaseCasRegisteredServiceStreamPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasRegisteredServiceHazelcastStreamPublisher.class);
    
    private final HazelcastInstance instance;
    private final String mapName;

    public CasRegisteredServiceHazelcastStreamPublisher(final HazelcastInstance instance,
                                                        final StringBean publisherId) {
        super(publisherId);
        this.instance = instance;
        this.mapName = instance.getConfig().getMapConfigs().keySet().iterator().next();
    }

    @Override
    protected void publishInternal(final RegisteredService service, final ApplicationEvent event) {
        final IMap<String, RegisteredServicesQueuedEvent> inst = instance.getMap(mapName);
        inst.set(service.getName(), getEventToPublish(service, event));
    }
}
