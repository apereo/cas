package org.apereo.cas.bucket4j.producer;

import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.bucket4j.consumer.DefaultBucketConsumer;
import org.apereo.cas.configuration.model.support.bucket4j.BaseBucket4jProperties;
import org.apereo.cas.configuration.model.support.bucket4j.Bucket4jBandwidthLimitProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.function.FunctionUtils;

import io.github.bucket4j.AbstractBucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.bucket4j.local.LocalBucketBuilder;
import lombok.experimental.SuperBuilder;
import lombok.val;

import java.util.List;

/**
 * This is {@link BucketProducer}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SuperBuilder
public class BucketProducer {

    private final BaseBucket4jProperties properties;

    private static LocalBucketBuilder getBucketLimits(final List<Bucket4jBandwidthLimitProperties> bandwidths) {
        val builder = Bucket.builder().withNanosecondPrecision();
        bandwidths.stream()
            .map(bandwidth -> {
                var limit = (Bandwidth) null;
                switch (bandwidth.getRefillStrategy()) {
                    case INTERVALLY:
                        limit = Bandwidth.classic(bandwidth.getCapacity(), Refill.intervally(bandwidth.getRefillCount(),
                            Beans.newDuration(bandwidth.getRefillDuration())));
                        break;
                    case GREEDY:
                    default:
                        limit = Bandwidth.simple(bandwidth.getCapacity(), Beans.newDuration(bandwidth.getDuration()))
                            .withInitialTokens(bandwidth.getInitialTokens() <= 0 ? bandwidth.getCapacity() : bandwidth.getInitialTokens());
                        break;
                }
                limit = limit.withInitialTokens(bandwidth.getInitialTokens() <= 0
                    ? bandwidth.getCapacity() : bandwidth.getInitialTokens());
                return limit;
            })
            .forEach(builder::addLimit);
        return builder;
    }

    /**
     * Produce bucket.
     *
     * @return the abstract bucket
     */
    public BucketConsumer produce() {
        return FunctionUtils.doAndReturn(properties.isEnabled(),
            () -> {
                val bucket = (AbstractBucket) getBucketLimits(properties.getBandwidth()).build();
                return new DefaultBucketConsumer(bucket, properties);
            }, BucketConsumer::permitAll);
    }
}
