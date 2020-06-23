package org.apereo.cas.services.publisher;

import org.apereo.cas.JmsQueueIdentifier;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;
import java.time.Instant;

/**
 * This is {@link DefaultCasRegisteredServiceStreamPublisher}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class DefaultCasRegisteredServiceStreamPublisher extends BaseCasRegisteredServiceStreamPublisher {

    private final DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>> distributedCacheManager;

    public DefaultCasRegisteredServiceStreamPublisher(final DistributedCacheManager instance,
                                                      final JmsQueueIdentifier publisherId) {
        super(publisherId);
        this.distributedCacheManager = instance;
    }

    @Override
    protected void handleCasRegisteredServiceDeletedEvent(final RegisteredService service, final ApplicationEvent event) {
        val item = getCacheObject(service, event);
        LOGGER.debug("Removing service [{}] from cache [{}] @ [{}]", service, distributedCacheManager.getName(), item.getTimestamp());
        this.distributedCacheManager.update(service, item);
    }

    @Override
    protected void handleCasRegisteredServiceUpdateEvents(final RegisteredService service, final ApplicationEvent event) {
        val item = getCacheObject(service, event);
        LOGGER.debug("Storing item [{}] to cache [{}] @ [{}]", item, distributedCacheManager.getName(), item.getTimestamp());
        this.distributedCacheManager.set(service, item);
    }

    private static DistributedCacheObject<RegisteredService> getCacheObject(final RegisteredService service,
                                                                            final ApplicationEvent event) {
        val time = Instant.now(Clock.systemUTC()).toEpochMilli();
        val item = new DistributedCacheObject<RegisteredService>(time, service);
        item.getProperties().put("event", event);
        return item;
    }
}
