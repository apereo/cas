package org.apereo.cas.bucket4j.consumer;

import module java.base;
import org.apereo.cas.bucket4j.producer.InMemoryBucketStore;
import org.apereo.cas.configuration.model.support.bucket4j.Bucket4jBandwidthLimitProperties;
import org.apereo.cas.configuration.model.support.throttle.Bucket4jThrottleProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultBucketConsumerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("AuthenticationThrottling")
class DefaultBucketConsumerTests {

    @Test
    void verifyFailureAsync() {
        val props = new Bucket4jThrottleProperties();
        props.setBlocking(false);
        props.getBandwidth().add(new Bucket4jBandwidthLimitProperties().setCapacity(1).setRefillDuration("PT1S"));
        val producer = new DefaultBucketConsumer(new InMemoryBucketStore(props), props);
        val key = UUID.randomUUID().toString();
        var result = producer.consume(key);
        assertTrue(result.isConsumed());
        result = producer.consume(key);
        assertFalse(result.isConsumed());
    }
}
