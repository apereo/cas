package org.apereo.cas.initializr.rate;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;

/**
 * The type Rate limit interceptor.
 */
public class RateLimitInterceptor implements HandlerInterceptor {
    private final Bucket tokenBucket;

    /**
     * 60 requests per minute.
     * As requests are consuming tokens, we are also replenishing them
     * at some fixed rate, such that we never exceed the capacity of the bucket.
     */
    public RateLimitInterceptor() {
        this.tokenBucket = Bucket4j.builder()
            .addLimit(Bandwidth.classic(60, Refill.intervally(60, Duration.ofMinutes(1))))
            .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        val probe = tokenBucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        }
        var waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
        response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
        response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Exhausted Request Quota");
        return false;
    }
}
