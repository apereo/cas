package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.throttle.Bucket4jThrottleProperties;
import org.apereo.cas.throttle.ThrottledRequestExecutor;
import org.apereo.cas.util.LoggingUtils;

import io.github.bucket4j.AbstractBucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BlockingStrategy;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link Bucket4jThrottledRequestExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class Bucket4jThrottledRequestExecutor implements ThrottledRequestExecutor {

    /**
     * Header value to indicate available tokens.
     */
    public static final String HEADER_NAME_X_RATE_LIMIT_REMAINING = "X-Rate-Limit-Remaining";

    /**
     * Header value to indicate available tokens once capacity is consumed..
     */
    public static final String HEADER_NAME_X_RATE_LIMIT_RETRY_AFTER_SECONDS = "X-Rate-Limit-Retry-After-Seconds";

    private static final long MAX_WAIT_NANOS = TimeUnit.HOURS.toNanos(1);

    private final AbstractBucket bucket;

    private final boolean blocking;

    public Bucket4jThrottledRequestExecutor(final Bucket4jThrottleProperties properties) {
        val duration = Duration.ofSeconds(properties.getRangeInSeconds());

        val limit = properties.getOverdraft() > 0
            ? Bandwidth.classic(properties.getOverdraft(), Refill.greedy(properties.getCapacity(), duration))
            : Bandwidth.simple(properties.getCapacity(), duration);

        this.bucket = (AbstractBucket) Bucket4j.builder()
            .addLimit(limit)
            .withMillisecondPrecision()
            .build();

        this.blocking = properties.isBlocking();
    }

    @Override
    public boolean throttle(final HttpServletRequest request, final HttpServletResponse response) {
        var result = true;

        val availableTokens = this.bucket.getAvailableTokens();
        try {
            if (this.blocking) {
                LOGGER.trace("Attempting to consume a token for the authentication attempt");
                result = !this.bucket.tryConsume(1, MAX_WAIT_NANOS, BlockingStrategy.PARKING);
            } else {
                result = !this.bucket.tryConsume(1);
            }
        } catch (final InterruptedException e) {
            LoggingUtils.error(LOGGER, e);
            Thread.currentThread().interrupt();
        }
        if (result) {
            val probe = this.bucket.tryConsumeAndReturnRemaining(1);
            val seconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
            response.addHeader(HEADER_NAME_X_RATE_LIMIT_RETRY_AFTER_SECONDS, Long.toString(seconds));
            LOGGER.warn("The request is throttled as capacity is entirely consumed. Available tokens are [{}]", availableTokens);
        } else {
            response.addHeader(HEADER_NAME_X_RATE_LIMIT_REMAINING, Long.toString(availableTokens));
        }
        return result;
    }
}
