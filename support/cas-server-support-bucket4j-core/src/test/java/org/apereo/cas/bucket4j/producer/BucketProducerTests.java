package org.apereo.cas.bucket4j.producer;

import org.apereo.cas.bucket4j.consumer.DefaultBucketConsumer;
import org.apereo.cas.configuration.model.support.bucket4j.Bucket4jBandwidthLimitProperties;
import org.apereo.cas.configuration.model.support.throttle.Bucket4jThrottleProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BucketProducerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("AuthenticationThrottling")
class BucketProducerTests {

    @Test
    void verifyOperation() {
        val limit1 = new Bucket4jBandwidthLimitProperties()
            .setDuration("PT15S")
            .setInitialTokens(100)
            .setRefillStrategy(Bucket4jBandwidthLimitProperties.BandwidthRefillStrategies.GREEDY);
        val limit2 = new Bucket4jBandwidthLimitProperties()
            .setRefillDuration("PT10S")
            .setRefillCount(50)
            .setRefillStrategy(Bucket4jBandwidthLimitProperties.BandwidthRefillStrategies.INTERVALLY);
        val props = new Bucket4jThrottleProperties().setBandwidth(List.of(limit1, limit2));
        val producer = new DefaultBucketConsumer(new InMemoryBucketStore(props), props);
        assertTrue(producer.consume(UUID.randomUUID().toString()).isConsumed());
    }

    @Test
    void verifyNoBucket() {
        val limit1 = new Bucket4jBandwidthLimitProperties()
            .setDuration("PT15S")
            .setInitialTokens(100)
            .setRefillStrategy(Bucket4jBandwidthLimitProperties.BandwidthRefillStrategies.GREEDY);
        val props = new Bucket4jThrottleProperties().setBandwidth(List.of(limit1));
        val store = mock(BucketStore.class);
        when(store.obtainBucket(anyString())).thenReturn(null);
        val producer = new DefaultBucketConsumer(store, props);
        assertFalse(producer.consume(UUID.randomUUID().toString()).isConsumed());
    }
}
