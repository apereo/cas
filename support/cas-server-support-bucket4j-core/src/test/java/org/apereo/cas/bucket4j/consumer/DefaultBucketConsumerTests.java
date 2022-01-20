package org.apereo.cas.bucket4j.consumer;

import org.apereo.cas.configuration.model.support.throttle.Bucket4jThrottleProperties;

import io.github.bucket4j.AbstractBucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultBucketConsumerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Simple")
public class DefaultBucketConsumerTests {

    @Test
    public void verifyFailure() {
        val bucket = mock(AbstractBucket.class);
        when(bucket.getAvailableTokens()).thenReturn(1L);
        when(bucket.tryConsume(anyLong())).thenThrow(new RuntimeException());

        val probe = mock(ConsumptionProbe.class);
        when(probe.getNanosToWaitForRefill()).thenReturn(100L);
        when(bucket.tryConsumeAndReturnRemaining(anyLong())).thenReturn(probe);

        val producer = new DefaultBucketConsumer(bucket, new Bucket4jThrottleProperties());
        val result = producer.consume();
        assertNotNull(result);
        assertFalse(result.isConsumed());
    }
}
