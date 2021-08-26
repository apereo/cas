package org.apereo.cas.services.publisher;

import org.apereo.cas.services.RegisteredService;
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

    @Override
    protected void handleCasRegisteredServiceUpdateEvents(final RegisteredService service, final ApplicationEvent event,
                                                          final PublisherIdentifier publisherId) {
        val item = getCacheObject(service, event, publisherId);
        LOGGER.debug("Storing item [{}] to cache [{}] @ [{}]", item, distributedCacheManager.getName(), item.getTimestamp());
        this.distributedCacheManager.set(service, item, true);
    }
}
