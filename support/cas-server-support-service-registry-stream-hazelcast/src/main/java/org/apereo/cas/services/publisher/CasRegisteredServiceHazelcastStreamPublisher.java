package org.apereo.cas.services.publisher;

import org.apereo.cas.StringBean;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.DistributedCacheManager;
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

    private final DistributedCacheManager<RegisteredService, RegisteredService, RegisteredServicesQueuedEvent> distributedCacheManager;

    public CasRegisteredServiceHazelcastStreamPublisher(final DistributedCacheManager instance,
                                                        final StringBean publisherId) {
        super(publisherId);
        this.distributedCacheManager = instance;
    }

    @Override
    protected void publishInternal(final RegisteredService service, final ApplicationEvent event) {
        final RegisteredServicesQueuedEvent eventToPublish = getEventToPublish(service, event);
        LOGGER.debug("Publishing event [{}] to cache [{}]", eventToPublish, this.distributedCacheManager.getName());
        this.distributedCacheManager.set(service, eventToPublish);
    }
}
