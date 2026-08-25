package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.cache.DistributedCacheObject;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaOperations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceKafkaDistributedCacheManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Kafka")
class RegisteredServiceKafkaDistributedCacheManagerTests {
    @Test
    void verifyOperation() {
        val kafkaTemplate = mock(KafkaOperations.class);
        doReturn(CompletableFuture.completedFuture(null))
            .when(kafkaTemplate).send(anyString(), anyString(), any());
        try (val manager = new RegisteredServiceKafkaDistributedCacheManager(kafkaTemplate, "registered-services")) {
            val service = RegisteredServiceTestUtils.getRegisteredService();
            assertFalse(manager.contains(service));
            assertTrue(manager.getAll().isEmpty());
            assertTrue(manager.findAll(Objects::nonNull).isEmpty());

            val item = new DistributedCacheObject<RegisteredService>(Map.of(),
                System.currentTimeMillis(),
                service, new PublisherIdentifier());
            assertNotNull(manager.set(service, item, true));
            assertNotNull(manager.set(service, item, false));

            assertNotNull(manager.update(service, item, true));
            assertNotNull(manager.update(service, item, false));
            verify(kafkaTemplate, times(4)).send(eq("registered-services"), anyString(), same(item));
        }
    }
}
