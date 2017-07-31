package org.apereo.cas.services.listener;

import com.hazelcast.core.EntryAdapter;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.MapEvent;
import org.apereo.cas.services.publisher.RegisteredServicesQueuedEvent;

/**
 * This is {@link HazelcastRegisteredServiceEventListener}.
 * Do not run complex and/or long running code off the event threading pool. 
 * The answer is to off-load the execution onto a user controlled thread pool as a delegate.
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class HazelcastRegisteredServiceEventListener extends EntryAdapter<String, RegisteredServicesQueuedEvent> {
    private final RegisteredServiceEntryEventService entryEventService;
    
    public HazelcastRegisteredServiceEventListener(final RegisteredServiceEntryEventService entryEventService) {
        this.entryEventService = entryEventService;
    }

    @Override
    public void entryAdded(final EntryEvent<String, RegisteredServicesQueuedEvent> entryEvent) {
        entryEventService.process(entryEvent);
    }

    @Override
    public void entryEvicted(final EntryEvent<String, RegisteredServicesQueuedEvent> entryEvent) {
        entryEventService.process(entryEvent);
    }

    @Override
    public void entryRemoved(final EntryEvent<String, RegisteredServicesQueuedEvent> entryEvent) {
        entryEventService.process(entryEvent);
    }

    @Override
    public void entryUpdated(final EntryEvent<String, RegisteredServicesQueuedEvent> entryEvent) {
        entryEventService.process(entryEvent);
    }

    @Override
    public void mapCleared(final MapEvent mapEvent) {
    }

    @Override
    public void mapEvicted(final MapEvent mapEvent) {
    }
}
