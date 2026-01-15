package org.apereo.cas.bucket4j.producer;

import module java.base;
import org.apereo.cas.configuration.model.support.bucket4j.BaseBucket4jProperties;
import org.apereo.cas.configuration.support.Beans;
import io.github.bucket4j.AbstractBucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.bucket4j.local.LocalBucketBuilder;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link InMemoryBucketStore}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class InMemoryBucketStore implements BucketStore {
    private final Map<String, AbstractBucket> store = new ConcurrentHashMap<>();

    private final BaseBucket4jProperties properties;

    @Override
    public AbstractBucket obtainBucket(final String key) {
        return store.computeIfAbsent(key, k -> (AbstractBucket) getBucketLimits().build());
    }

    private LocalBucketBuilder getBucketLimits() {
        val builder = Bucket.builder().withNanosecondPrecision();
        properties.getBandwidth()
            .stream()
            .map(bandwidth -> {
                var limit = switch (bandwidth.getRefillStrategy()) {
                    case INTERVALLY -> Bandwidth.classic(bandwidth.getCapacity(), Refill.intervally(bandwidth.getRefillCount(),
                        Beans.newDuration(bandwidth.getRefillDuration())));
                    case GREEDY -> Bandwidth.simple(bandwidth.getCapacity(), Beans.newDuration(bandwidth.getDuration()))
                        .withInitialTokens(bandwidth.getInitialTokens() <= 0 ? bandwidth.getCapacity() : bandwidth.getInitialTokens());
                };
                limit = limit.withInitialTokens(bandwidth.getInitialTokens() <= 0
                    ? bandwidth.getCapacity() : bandwidth.getInitialTokens());
                return limit;
            })
            .forEach(builder::addLimit);
        return builder;
    }
}
