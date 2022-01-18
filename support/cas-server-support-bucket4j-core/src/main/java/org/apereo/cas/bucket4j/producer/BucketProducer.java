package org.apereo.cas.bucket4j.producer;

import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.bucket4j.consumer.DefaultBucketConsumer;
import org.apereo.cas.configuration.model.support.bucket4j.BaseBucket4jProperties;

import io.github.bucket4j.AbstractBucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.experimental.SuperBuilder;
import lombok.val;

import java.time.Duration;

/**
 * This is {@link BucketProducer}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SuperBuilder
public class BucketProducer {

    private final BaseBucket4jProperties properties;

    /**
     * Produce bucket.
     *
     * @return the abstract bucket
     */
    public BucketConsumer produce() {
        val duration = Duration.ofSeconds(properties.getRangeInSeconds());
        val limit = properties.getOverdraft() > 0
            ? Bandwidth.classic(properties.getOverdraft(), Refill.greedy(properties.getCapacity(), duration))
            : Bandwidth.simple(properties.getCapacity(), duration);
        val bucket = (AbstractBucket) Bucket.builder()
            .addLimit(limit)
            .withMillisecondPrecision()
            .build();
        return new DefaultBucketConsumer(bucket, properties);
    }
}
