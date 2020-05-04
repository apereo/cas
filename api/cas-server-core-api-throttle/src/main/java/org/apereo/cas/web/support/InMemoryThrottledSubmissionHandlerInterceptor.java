package org.apereo.cas.web.support;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link InMemoryThrottledSubmissionHandlerInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface InMemoryThrottledSubmissionHandlerInterceptor extends ThrottledSubmissionHandlerInterceptor {

    /**
     * Construct key to be used by the throttling agent to track requests.
     *
     * @param request the request
     * @return the key
     */
    String constructKey(HttpServletRequest request);

}
