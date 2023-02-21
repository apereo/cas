package org.apereo.cas.throttle;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link ThrottledRequestResponseHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface ThrottledRequestResponseHandler {

    /**
     * Default Bean name.
     */
    String BEAN_NAME = "throttledRequestResponseHandler";

    /**
     * Handle.
     *
     * @param request  the request
     * @param response the response
     * @return false to stop the request. true to proceed anyway.
     */
    boolean handle(HttpServletRequest request, HttpServletResponse response);
}
