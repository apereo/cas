package org.apereo.cas.web.support;

import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link ThrottledSubmissionHandlerInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface ThrottledSubmissionHandlerInterceptor extends AsyncHandlerInterceptor {

    /**
     * Record submission failure.
     *
     * @param request the request
     */
    default void recordSubmissionFailure(final HttpServletRequest request) {
    }

    /**
     * Determine whether threshold has been exceeded.
     *
     * @param request the request
     * @return true, if successful
     */
    default boolean exceedsThreshold(final HttpServletRequest request) {
        return false;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Decrement the the throttle so authentication can resume.
     */
    default void decrement() {
    }

    @Override
    default boolean preHandle(final HttpServletRequest request,
                              final HttpServletResponse response,
                              final Object handler) throws Exception {
        return true;
    }

    @Override
    default void postHandle(final HttpServletRequest request,
                            final HttpServletResponse response,
                            final Object handler,
                            final ModelAndView modelAndView) {
    }

    @Override
    default void afterConcurrentHandlingStarted(final HttpServletRequest request,
                                                final HttpServletResponse response,
                                                final Object handler) throws Exception {
    }

    @Override
    default void afterCompletion(final HttpServletRequest request,
                                 final HttpServletResponse response,
                                 final Object handler,
                                 final Exception e) throws Exception {
    }

    /**
     * No op throttled submission handler interceptor.
     *
     * @return the throttled submission handler interceptor
     */
    static ThrottledSubmissionHandlerInterceptor noOp() {
        return new ThrottledSubmissionHandlerInterceptor() {
        };
    }
}
