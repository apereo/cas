package org.apereo.cas.bucket4j.consumer;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link BucketConsumer}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface BucketConsumer {
    /**
     * Header value to indicate available tokens.
     */
    String HEADER_NAME_X_RATE_LIMIT_REMAINING = "X-Rate-Limit-Remaining";

    /**
     * Header value to indicate available tokens once capacity is consumed..
     */
    String HEADER_NAME_X_RATE_LIMIT_RETRY_AFTER_SECONDS = "X-Rate-Limit-Retry-After-Seconds";

    /**
     * limit of time(in nanoseconds) which thread can wait for a blocking call.
     */
    long MAX_WAIT_NANOS = TimeUnit.HOURS.toNanos(1);

    /**
     * Consume bucket and return consumption result.
     *
     * @return the bucket consumption result
     */
    BucketConsumptionResult consume();

    /**
     * Permit all bucket consumer.
     *
     * @return the bucket consumer
     */
    static BucketConsumer permitAll() {
        return () -> BucketConsumptionResult.builder().consumed(true).build();
    }
}
