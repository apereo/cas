package org.apereo.cas.throttle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link ThrottledRequestResponseHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface ThrottledRequestResponseHandler {

    /**
     * Handle.
     *
     * @param request  the request
     * @param response the response
     * @return false to stop the request. true to proceed anyway.
     */
    boolean handle(HttpServletRequest request, HttpServletResponse response);
}
