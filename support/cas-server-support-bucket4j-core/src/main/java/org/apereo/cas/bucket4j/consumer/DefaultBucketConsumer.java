package org.apereo.cas.bucket4j.consumer;

import org.apereo.cas.configuration.model.support.bucket4j.BaseBucket4jProperties;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;

import io.github.bucket4j.AbstractBucket;
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
    private final AbstractBucket bucket;

    private final BaseBucket4jProperties properties;

    @Override
    public BucketConsumptionResult consume() {
        val availableTokens = this.bucket.getAvailableTokens();
        val canProceed = FunctionUtils.doAndHandle(() -> {
            if (properties.isBlocking()) {
                LOGGER.trace("Attempting to consume a token for the authentication attempt");
                return bucket.tryConsume(1, MAX_WAIT_NANOS, BlockingStrategy.PARKING);
            }
            return bucket.tryConsume(1);
        }, e -> {
            LoggingUtils.error(LOGGER, e);
            Thread.currentThread().interrupt();
            return false;
        }).get();

        val headers = new LinkedHashMap<String, String>();
        if (!canProceed) {
            val probe = this.bucket.tryConsumeAndReturnRemaining(1);
            val seconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
            headers.put(HEADER_NAME_X_RATE_LIMIT_RETRY_AFTER_SECONDS, Long.toString(seconds));
            LOGGER.warn("The request is throttled as capacity is entirely consumed. Available tokens are [{}]", availableTokens);
            return BucketConsumptionResult.builder().consumed(false).headers(headers).build();
        }
        headers.put(HEADER_NAME_X_RATE_LIMIT_REMAINING, Long.toString(availableTokens));
        return BucketConsumptionResult.builder().consumed(true).headers(headers).build();
    }
}
