package org.apereo.cas.throttle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link ThrottledRequestExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public interface ThrottledRequestExecutor {

    /**
     * Attempts to pre-handle and throttle/stifle the requests
     * based on capacity and configured pools.
     *
     * @param request  the request
     * @param response the response
     * @return true to stop the throttled request. false to proceed anyway.
     */
    default boolean throttle(final HttpServletRequest request, final HttpServletResponse response) {
        return false;
    }

    /**
     * NoOp throttled request executor.
     *
     * @return the throttled request executor
     */
    static ThrottledRequestExecutor noOp() {
        return new ThrottledRequestExecutor() {
        };
    }
}
