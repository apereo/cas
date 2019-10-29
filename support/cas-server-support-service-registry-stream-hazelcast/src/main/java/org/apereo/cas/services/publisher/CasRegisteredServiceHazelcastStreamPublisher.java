package org.apereo.cas.services.publisher;

import org.apereo.cas.JmsQueueIdentifier;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEvent;

import java.util.Date;

/**
 * This is {@link CasRegisteredServiceHazelcastStreamPublisher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class CasRegisteredServiceHazelcastStreamPublisher extends BaseCasRegisteredServiceStreamPublisher {


    private final DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>> distributedCacheManager;

    public CasRegisteredServiceHazelcastStreamPublisher(final DistributedCacheManager instance,
                                                        final JmsQueueIdentifier publisherId) {
        super(publisherId);
        this.distributedCacheManager = instance;
    }

    @Override
    protected void handleCasRegisteredServiceDeletedEvent(final RegisteredService service, final ApplicationEvent event) {
        val item = getCacheObject(service, event);
        LOGGER.debug("Removing service [{}] from cache [{}] @ [{}]", service, this.distributedCacheManager.getName(), item.getTimestamp());
        this.distributedCacheManager.update(service, item);
    }

    @Override
    protected void handleCasRegisteredServiceUpdateEvents(final RegisteredService service, final ApplicationEvent event) {
        val item = getCacheObject(service, event);
        LOGGER.debug("Storing item [{}] to cache [{}] @ [{}]", item, this.distributedCacheManager.getName(), item.getTimestamp());
        this.distributedCacheManager.set(service, item);
    }

    private static DistributedCacheObject<RegisteredService> getCacheObject(final RegisteredService service, final ApplicationEvent event) {
        val time = new Date().getTime();
        val item = new DistributedCacheObject<RegisteredService>(time, service);
        item.getProperties().put("event", event);
        return item;
    }
}
