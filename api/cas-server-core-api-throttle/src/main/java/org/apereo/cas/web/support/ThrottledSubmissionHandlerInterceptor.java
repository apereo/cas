package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.util.NamedObject;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link ThrottledSubmissionHandlerInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface ThrottledSubmissionHandlerInterceptor extends AsyncHandlerInterceptor, NamedObject {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "authenticationThrottle";

    /**
     * No op throttled submission handler interceptor.
     *
     * @return the throttled submission handler interceptor
     */
    static ThrottledSubmissionHandlerInterceptor noOp() {
        return new ThrottledSubmissionHandlerInterceptor() {
        };
    }

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
     * Decrement the throttle so authentication can resume.
     */
    default void release() {
    }

    /**
     * Clear records and remove all.
     */
    default void clear() {
    }

    @Override
    default boolean preHandle(
        final @NonNull HttpServletRequest request,
        final @NonNull HttpServletResponse response,
        final @NonNull Object handler) {
        return true;
    }

    @Override
    default void postHandle(
        final @NonNull HttpServletRequest request,
        final @NonNull HttpServletResponse response,
        final @NonNull Object handler,
        final ModelAndView modelAndView) {
    }

    @Override
    default void afterCompletion(
        final @NonNull HttpServletRequest request,
        final @NonNull HttpServletResponse response,
        final @NonNull Object handler,
        final Exception e) {
    }

    @Override
    default void afterConcurrentHandlingStarted(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final Object handler) {
    }
}
