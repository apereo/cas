package org.apereo.cas.services.publisher;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEvent;

/**
 * This is {@link DefaultCasRegisteredServiceStreamPublisher}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultCasRegisteredServiceStreamPublisher extends BaseCasRegisteredServiceStreamPublisher {

    private final DistributedCacheManager<RegisteredService,
        DistributedCacheObject<RegisteredService>,
        PublisherIdentifier> distributedCacheManager;

    private static DistributedCacheObject getCacheObject(final RegisteredService service,
                                                         final ApplicationEvent event,
                                                         final PublisherIdentifier publisherId) {
        return DistributedCacheObject.<RegisteredService>builder()
            .value(service)
            .publisherIdentifier(publisherId)
            .properties(CollectionUtils.wrap("event", event.getClass().getSimpleName()))
            .build();
    }

    @Override
    protected void handleCasRegisteredServiceDeletedEvent(final RegisteredService service, final ApplicationEvent event,
                                                          final PublisherIdentifier publisherId) {
        val item = getCacheObject(service, event, publisherId);
        LOGGER.debug("Removing service [{}] from cache [{}] @ [{}]", service, distributedCacheManager.getName(), item.getTimestamp());
        this.distributedCacheManager.update(service, item, true);
    }

    /**
     * Store the service in the cache. Services are loaded again on every reload of the registry,
     * so a loaded service that the cache already holds unchanged is not sent again; saved services
     * are always sent.
     *
     * @param service     the service
     * @param event       the event
     * @param publisherId the publisher id
     */
    @Override
    protected void handleCasRegisteredServiceUpdateEvents(final RegisteredService service, final ApplicationEvent event,
                                                          final PublisherIdentifier publisherId) {
        if (event instanceof CasRegisteredServiceLoadedEvent && isCachedUnchanged(service)) {
            LOGGER.trace("Service [{}] is already in cache [{}] unchanged", service.getName(), distributedCacheManager.getName());
            return;
        }
        val item = getCacheObject(service, event, publisherId);
        LOGGER.debug("Storing item [{}] to cache [{}] @ [{}]", item, distributedCacheManager.getName(), item.getTimestamp());
        this.distributedCacheManager.set(service, item, true);
    }

    private boolean isCachedUnchanged(final RegisteredService service) {
        val cached = distributedCacheManager.get(service);
        return cached != null
            && service.equals(cached.getValue())
            && !CasRegisteredServiceDeletedEvent.class.getSimpleName().equals(cached.getProperties().get("event"));
    }
}
