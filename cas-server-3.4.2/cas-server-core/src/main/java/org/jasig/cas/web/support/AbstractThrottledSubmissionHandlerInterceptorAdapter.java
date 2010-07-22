/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Abstract implementation of the handler that has all of the logic.  Encapsulates the logic in case we get it wrong!
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.3.5
 */
public abstract class AbstractThrottledSubmissionHandlerInterceptorAdapter extends HandlerInterceptorAdapter {

    private static final int DEFAULT_FAILURE_THRESHOLD = 100;

    private static final int DEFAULT_FAILURE_RANGE_IN_SECONDS = 60;

    private static final String DEFAULT_USERNAME_PARAMETER = "username";

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Min(0)
    private int failureThreshold = DEFAULT_FAILURE_THRESHOLD;

    @Min(0)
    private int failureRangeInSeconds = DEFAULT_FAILURE_RANGE_IN_SECONDS;

    @NotNull
    private String usernameParameter = DEFAULT_USERNAME_PARAMETER;

    @Override
    public final boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object o) throws Exception {
        // we only care about post because that's the only instance where we can get anything useful besides IP address.
        if (!"POST".equals(request.getMethod())) {
            return true;
        }

        final int count = findCount(request, this.usernameParameter, this.failureRangeInSeconds);

        if (count >= this.failureThreshold) {
            updateCount(request, this.usernameParameter);
            log.warn("*** Possible Hacking Attempt from [" + request.getRemoteAddr() + "].  More than " + this.failureThreshold + " failed login attempts within " + this.failureRangeInSeconds + " seconds.");
            response.sendError(403, "Access Denied for user [" + request.getParameter(usernameParameter) + " from IP Address [" + request.getRemoteAddr() + "]");
            return false;
        }

        return true;
    }

    @Override
    public final void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object o, final ModelAndView modelAndView) throws Exception {
        if (!"POST".equals(request.getMethod())) {
            return;
        }

        // this means that the authentication succeeded, I think
        if (modelAndView == null || !"casLoginView".equals(modelAndView.getViewName())) {
            return;
        }

        updateCount(request, this.usernameParameter);
    }

    protected abstract int findCount(HttpServletRequest request, final String usernameParameter, int failureRangeInSeconds);

    protected abstract void updateCount(HttpServletRequest request, String usernameParameter);

    public final void setFailureThreshold(final int failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    public final void setFailureRangeInSeconds(final int failureRangeInSeconds) {
        this.failureRangeInSeconds = failureRangeInSeconds;
    }

    public final void setUsernameParameter(final String usernameParameter) {
        this.usernameParameter = usernameParameter;
    }
}
