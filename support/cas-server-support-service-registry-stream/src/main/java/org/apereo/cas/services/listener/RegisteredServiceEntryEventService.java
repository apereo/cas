package org.apereo.cas.services.listener;


/**
 * This is {@link RegisteredServiceEntryEventService}.
 * <p>
 * Framework to provide encapsulation of EntryEvent processing.  Can be used to provide offloading of processing from Hazelcast event threads.
 * EntryEvents are passed off to the EntryEventService via the process method.
 * The EntryEventService then selects the appropriate EntryEventProcessor.
 * If an EntryEvent is passed for processing and a EntryEventTypeProcessor is not found for that EntryEventType then a
 * EntryEventServiceException is passed to the caller via the CompletionListener.onException()
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface RegisteredServiceEntryEventService {
    /**
     * Process.
     *
     * @param entryEvent the entry event
     */
    void process(Object entryEvent);
}
