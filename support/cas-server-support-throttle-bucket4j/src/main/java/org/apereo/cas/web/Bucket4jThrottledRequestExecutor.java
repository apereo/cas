package org.apereo.cas.web;

import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.throttle.ThrottledRequestExecutor;

import lombok.RequiredArgsConstructor;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link Bucket4jThrottledRequestExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class Bucket4jThrottledRequestExecutor implements ThrottledRequestExecutor {
    private final BucketConsumer bucketConsumer;

    @Override
    public boolean throttle(final HttpServletRequest request, final HttpServletResponse response) {
        val result = bucketConsumer.consume();
        result.getHeaders().forEach(response::addHeader);
        return result.isConsumed();
    }
}
