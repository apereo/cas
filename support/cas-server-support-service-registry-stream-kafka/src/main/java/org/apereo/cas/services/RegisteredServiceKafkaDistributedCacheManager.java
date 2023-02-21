package org.apereo.cas.services;

import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;
import org.apereo.cas.util.cache.MappableDistributedCacheManager;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.kafka.core.KafkaOperations;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link RegisteredServiceKafkaDistributedCacheManager}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class RegisteredServiceKafkaDistributedCacheManager extends
    MappableDistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>> {

    private final KafkaOperations<String, DistributedCacheObject<RegisteredService>> kafkaTemplate;

    private final String topic;

    public RegisteredServiceKafkaDistributedCacheManager(
        final KafkaOperations<String, DistributedCacheObject<RegisteredService>> kafkaTemplate, final String topic) {
        super(new ConcurrentHashMap<>());
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    @CanIgnoreReturnValue
    public DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier>
        set(final RegisteredService key, final DistributedCacheObject<RegisteredService> item,
            final boolean publish) {
        if (publish) {
            sendObject(key, item);
        }
        return super.set(key, item, publish);
    }

    @Override
    @CanIgnoreReturnValue
    public DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier>
        update(final RegisteredService key, final DistributedCacheObject<RegisteredService> item,
               final boolean publish) {
        if (publish) {
            sendObject(key, item);
        }
        return super.update(key, item, publish);
    }

    @Override
    @CanIgnoreReturnValue
    public DistributedCacheManager<RegisteredService, DistributedCacheObject<RegisteredService>, PublisherIdentifier>
        remove(final RegisteredService key, final DistributedCacheObject<RegisteredService> item, final boolean publish) {
        if (publish) {
            sendObject(key, item);
        }
        return super.remove(key, item, publish);
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    private void sendObject(final RegisteredService key, final DistributedCacheObject<RegisteredService> item) {
        val itemKey = buildKey(key);
        val future = kafkaTemplate.send(topic, itemKey, item);
        future.whenComplete((result, ex) -> {
            LOGGER.trace("Published [{}]", result);
            LoggingUtils.error(LOGGER, ex);
        });
    }
}
