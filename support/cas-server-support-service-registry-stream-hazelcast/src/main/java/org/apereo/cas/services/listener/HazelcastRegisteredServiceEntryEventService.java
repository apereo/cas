package org.apereo.cas.services.listener;

import com.hazelcast.core.EntryEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.listener.handlers.RegisteredServiceEntryAddedEventHandler;
import org.apereo.cas.StringBean;
import org.apereo.cas.services.publisher.RegisteredServicesQueuedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * This is {@link HazelcastRegisteredServiceEntryEventService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class HazelcastRegisteredServiceEntryEventService extends BaseThreadedRegisteredServiceEntryEventService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastRegisteredServiceEntryEventService.class);
    
    public HazelcastRegisteredServiceEntryEventService(final ExecutorService executor, 
                                                       final StringBean publisher, 
                                                       final ServicesManager servicesManager) {
        super(executor, publisher, servicesManager);
    }

    @Override
    protected Optional<Pair<RegisteredServicesQueuedEvent, Runnable>> getQueuedEvent(final Object hzEntry) {
        final EntryEvent event = EntryEvent.class.cast(hzEntry);
        Optional<Pair<RegisteredServicesQueuedEvent, Runnable>> result = Optional.empty();
        
        if (event == null) {
            return result;    
        }
        
        switch (event.getEventType()) {
            case EVICTED:
                final RegisteredServicesQueuedEvent eventEvicted = RegisteredServicesQueuedEvent.class.cast(event.getOldValue());
                LOGGER.debug("Service definition [{}] is evicted from the Hazelcast map", eventEvicted.getService().getServiceId());
                break;
            case ADDED:
            case UPDATED:
                final RegisteredServicesQueuedEvent eventAdded = RegisteredServicesQueuedEvent.class.cast(event.getValue());
                result = Optional.of(Pair.of(eventAdded, new RegisteredServiceEntryAddedEventHandler(eventAdded, servicesManager)));
                break;
            default:
                LOGGER.warn("Event [{}] is not supported for Hazelcast service definition event queues", hzEntry);
        }
        return result;
    }
}
