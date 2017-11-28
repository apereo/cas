package org.apereo.cas.web.support;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract implementation of the handler that has all of the logic.  Encapsulates the logic in case we get it wrong!
 *
 * @author Scott Battaglia
 * @since 3.3.5
 */
public abstract class AbstractThrottledSubmissionHandlerInterceptorAdapter
        extends HandlerInterceptorAdapter implements ThrottledSubmissionHandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractThrottledSubmissionHandlerInterceptorAdapter.class);
    
    private final int failureThreshold;
    private final int failureRangeInSeconds;
    private final String usernameParameter;

    private double thresholdRate;

    public AbstractThrottledSubmissionHandlerInterceptorAdapter(final int failureThreshold, final int failureRangeInSeconds, final String usernameParameter) {
        this.failureThreshold = failureThreshold;
        this.failureRangeInSeconds = failureRangeInSeconds;
        this.usernameParameter = usernameParameter;
    }

    /**
     * Configure the threshold rate.
     */
    @PostConstruct
    public void afterPropertiesSet() {
        this.thresholdRate = this.failureThreshold / (double) this.failureRangeInSeconds;
        LOGGER.debug("Calculated threshold rate as [{}]", this.thresholdRate);
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object o) throws Exception {
        // we only care about post because that's the only instance where we can get anything useful besides IP address.
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            return true;
        }

        if (exceedsThreshold(request)) {
            recordThrottle(request);
            request.setAttribute(WebUtils.CAS_ACCESS_DENIED_REASON, "screen.blocked.message");
            response.sendError(HttpStatus.SC_LOCKED,
                    "Access Denied for user [" + StringEscapeUtils.escapeHtml4(request.getParameter(this.usernameParameter))
                    + "] from IP Address [" + request.getRemoteAddr() + ']');
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                                 final Object o, final ModelAndView modelAndView) {
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            return;
        }

        final boolean recordEvent = response.getStatus() != HttpStatus.SC_CREATED
                                 && response.getStatus() != HttpStatus.SC_OK
                                 && response.getStatus() != HttpStatus.SC_MOVED_TEMPORARILY;

        if (recordEvent) {
            LOGGER.debug("Recording submission failure for [{}]", request.getRequestURI());
            recordSubmissionFailure(request);
        }
    }

    protected double getThresholdRate() {
        return this.thresholdRate;
    }

    protected int getFailureThreshold() {
        return this.failureThreshold;
    }

    protected int getFailureRangeInSeconds() {
        return this.failureRangeInSeconds;
    }

    protected String getUsernameParameter() {
        return this.usernameParameter;
    }

    /**
     * Record throttling event.
     *
     * @param request the request
     */
    protected void recordThrottle(final HttpServletRequest request) {
        LOGGER.warn("Throttling submission from [{}]. More than [{}] failed login attempts within [{}] seconds. "
                + "Authentication attempt exceeds the failure threshold [{}]",
                request.getRemoteAddr(), this.failureThreshold, this.failureRangeInSeconds,
                this.failureThreshold);
    }
        
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("failureThreshold", this.failureThreshold)
                .append("failureRangeInSeconds", this.failureRangeInSeconds)
                .append("usernameParameter", this.usernameParameter)
                .append("thresholdRate", this.thresholdRate)
                .toString();
    }

    @Override
    public void decrement() {
        LOGGER.debug("Throttling is not activated for this interceptor adapter");
    }
}
