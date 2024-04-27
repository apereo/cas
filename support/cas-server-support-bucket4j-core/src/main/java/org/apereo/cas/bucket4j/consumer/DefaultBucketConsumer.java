package org.apereo.cas.bucket4j.consumer;

import org.apereo.cas.bucket4j.producer.BucketStore;
import org.apereo.cas.configuration.model.support.bucket4j.BaseBucket4jProperties;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.function.FunctionUtils;

import io.github.bucket4j.BlockingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link DefaultBucketConsumer}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultBucketConsumer implements BucketConsumer {
    private final CasReentrantLock lock = new CasReentrantLock();

    private final BucketStore bucketStore;

    private final BaseBucket4jProperties properties;

    @Override
    public BucketConsumptionResult consume(final String key) {
        return lock.tryLock(() -> {
            val bucket = bucketStore.obtainBucket(key);
            if (bucket == null) {
                LOGGER.warn("Unable to obtain a bucket for [{}]", key);
                return BucketConsumptionResult.builder().consumed(false).build();
            }

            val canProceed = FunctionUtils.doAndHandle(() -> {
                if (properties.isBlocking()) {
                    LOGGER.debug("Attempting to consume a token for the authentication attempt");
                    return bucket.tryConsume(1, MAX_WAIT_NANOS, BlockingStrategy.PARKING);
                }
                return bucket.tryConsume(1);
            }, e -> {
                LoggingUtils.error(LOGGER, e);
                Thread.currentThread().interrupt();
                return false;
            }).get();

            val headers = new LinkedHashMap<String, String>();
            val availableTokens = bucket.getAvailableTokens();
            if (!canProceed) {
                val probe = bucket.tryConsumeAndReturnRemaining(1);
                val seconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
                headers.put(HEADER_NAME_X_RATE_LIMIT_RETRY_AFTER_SECONDS, Long.toString(seconds));
                LOGGER.warn("The request is throttled as capacity is entirely consumed. Available tokens are [{}]", availableTokens);
                return BucketConsumptionResult.builder().consumed(false).headers(headers).build();
            }
            headers.put(HEADER_NAME_X_RATE_LIMIT_REMAINING, Long.toString(availableTokens));
            return BucketConsumptionResult.builder().consumed(true).headers(headers).build();
        });
    }
}
