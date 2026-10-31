package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;

/**
 * This is {@link RegisteredServiceKafkaDistributedCacheListener}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Getter
public class RegisteredServiceKafkaDistributedCacheListener {
    private final PublisherIdentifier publisherIdentifier;

    private final DistributedCacheManager<RegisteredService,
        DistributedCacheObject<RegisteredService>,
        PublisherIdentifier> cacheManager;

    /**
     * Registered service distributed cache kafka listener. Entries published by other nodes are stored
     * as they are, deletion markers included, so that loading services on this node removes a service
     * deleted elsewhere instead of sending it back.
     *
     * @param item the item
     */
    @KafkaListener(topics = "#{registeredServiceDistributedCacheKafkaTopic.name()}",
        groupId = "#{casRegisteredServiceStreamPublisherIdentifier.getId()}",
        containerFactory = "registeredServiceKafkaListenerContainerFactory")
    public void registeredServiceDistributedCacheKafkaListener(
        @Payload
        final DistributedCacheObject<RegisteredService> item) {
        if (!item.getPublisherIdentifier().getId().equals(publisherIdentifier.getId())) {
            cacheManager.update(item.getValue(), item, false);
        }
    }
}
