package org.apereo.cas.monitor;

import module java.base;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link SlowRequestsFilter}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class SlowRequestsFilter extends OncePerRequestFilter {
    private static final int SLOW_REQUEST_THRESHOLD = 5000;
    private final Timer slowRequestTimer;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain) {
        slowRequestTimer.record(Unchecked.runnable(() -> {
            val startTime = System.currentTimeMillis();
            filterChain.doFilter(request, response);
            val duration = System.currentTimeMillis() - startTime;
            if (duration > SLOW_REQUEST_THRESHOLD) {
                LOGGER.warn("Slow request detected: [{}] took [{}] ms", request.getRequestURI(), duration);
            }
        }));
    }
}
