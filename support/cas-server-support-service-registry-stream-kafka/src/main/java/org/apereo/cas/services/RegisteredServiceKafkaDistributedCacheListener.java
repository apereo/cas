package org.apereo.cas.services;

import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;

/**
 * This is {@link RegisteredServiceKafkaDistributedCacheListener}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class RegisteredServiceKafkaDistributedCacheListener {
    private final PublisherIdentifier publisherIdentifier;

    private final DistributedCacheManager<RegisteredService,
        DistributedCacheObject<RegisteredService>,
        PublisherIdentifier> cacheManager;

    @KafkaListener(topics = "#{registeredServiceDistributedCacheKafkaTopic.name()}",
        groupId = "registeredServices", containerFactory = "registeredServiceKafkaListenerContainerFactory")
    public void registeredServiceDistributedCacheKafkaListener(@Payload final DistributedCacheObject<RegisteredService> item) {
        if (!item.getPublisherIdentifier().getId().equals(publisherIdentifier.getId())) {
            if (!deleteObjectFromCache(item)) {
                cacheManager.update(item.getValue(), item, false);
            }
        }
    }

    private boolean deleteObjectFromCache(final DistributedCacheObject<RegisteredService> item) {
        if (item.containsProperty("event")) {
            val event = item.getProperty("event", String.class);
            if (event.equalsIgnoreCase(CasRegisteredServiceDeletedEvent.class.getSimpleName())) {
                cacheManager.remove(item.getValue(), item, false);
                return true;
            }
        }
        return false;
    }

}
