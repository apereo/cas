package org.apereo.cas.services.listener;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.StringBean;
import org.apereo.cas.services.publisher.RegisteredServicesQueuedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * This is {@link BaseThreadedRegisteredServiceEntryEventService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseThreadedRegisteredServiceEntryEventService implements RegisteredServiceEntryEventService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseThreadedRegisteredServiceEntryEventService.class);

    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;
    
    private final ExecutorService executor;
    private final StringBean publisherIdentifier;
    
    public BaseThreadedRegisteredServiceEntryEventService(final ExecutorService executor,
                                                          final StringBean publisher,
                                                          final ServicesManager servicesManager) {
        this.executor = executor;
        this.publisherIdentifier = publisher;
        this.servicesManager = servicesManager;
    }

    /**
     * Destroy and shut down the thread pool.
     */
    @PreDestroy
    public void destroy() {
        executor.shutdownNow();
    }

    @Override
    public final void process(final Object eventObject) {
        LOGGER.debug("Received entry event [{}]", eventObject);
        final Optional<Pair<RegisteredServicesQueuedEvent, Runnable>> event = getQueuedEvent(eventObject);
        if (event.isPresent()) {
            final Pair<RegisteredServicesQueuedEvent, Runnable> svcEvent = event.get();
            if (!svcEvent.getKey().getPublisher().equals(publisherIdentifier)) {
                this.executor.submit(svcEvent.getValue());
            } else {
                LOGGER.trace("Ignoring inbound event [{}] as the CAS publisher and listener are one of the same instance", eventObject);
            }
        }
    }

    /**
     * Gets queued event.
     *
     * @param entryEvent the entry event
     * @return the queued event
     */
    protected abstract Optional<Pair<RegisteredServicesQueuedEvent, Runnable>> getQueuedEvent(Object entryEvent);
    
}
