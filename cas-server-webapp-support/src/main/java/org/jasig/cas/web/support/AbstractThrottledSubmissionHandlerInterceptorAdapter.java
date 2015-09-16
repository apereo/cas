/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.web.support;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Abstract implementation of the handler that has all of the logic.  Encapsulates the logic in case we get it wrong!
 *
 * @author Scott Battaglia
 * @since 3.3.5
 */
public abstract class AbstractThrottledSubmissionHandlerInterceptorAdapter extends HandlerInterceptorAdapter implements InitializingBean {

    private static final int DEFAULT_FAILURE_THRESHOLD = 100;

    private static final int DEFAULT_FAILURE_RANGE_IN_SECONDS = 60;

    private static final String DEFAULT_USERNAME_PARAMETER = "username";

    private static final String SUCCESSFUL_AUTHENTICATION_EVENT = "success";

    /** Logger object. **/
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Min(0)
    private int failureThreshold = DEFAULT_FAILURE_THRESHOLD;

    @Min(0)
    private int failureRangeInSeconds = DEFAULT_FAILURE_RANGE_IN_SECONDS;

    @NotNull
    private String usernameParameter = DEFAULT_USERNAME_PARAMETER;

    private double thresholdRate;


    @Override
    public void afterPropertiesSet() throws Exception {
        this.thresholdRate = (double) failureThreshold / (double) failureRangeInSeconds;
    }


    @Override
    public final boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object o) throws Exception {
        // we only care about post because that's the only instance where we can get anything useful besides IP address.
        if (!"POST".equals(request.getMethod())) {
            return true;
        }

        if (exceedsThreshold(request)) {
            recordThrottle(request);
            request.setAttribute(WebUtils.CAS_ACCESS_DENIED_REASON, "screen.blocked.message");
            response.sendError(HttpStatus.SC_FORBIDDEN,
                    "Access Denied for user [" + request.getParameter(usernameParameter)
                    + "] from IP Address [" + request.getRemoteAddr() + ']');
            return false;
        }

        return true;
    }

    @Override
    public final void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                                 final Object o, final ModelAndView modelAndView) throws Exception {
        if (!"POST".equals(request.getMethod())) {
            return;
        }

        final RequestContext context = (RequestContext) request.getAttribute("flowRequestContext");

        if (context == null || context.getCurrentEvent() == null) {
            return;
        }

        // User successfully authenticated
        if (SUCCESSFUL_AUTHENTICATION_EVENT.equals(context.getCurrentEvent().getId())) {
            return;
        }

        // User submitted invalid credentials, so we update the invalid login count
        recordSubmissionFailure(request);
    }

    public final void setFailureThreshold(final int failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    public final void setFailureRangeInSeconds(final int failureRangeInSeconds) {
        this.failureRangeInSeconds = failureRangeInSeconds;
    }

    public final void setUsernameParameter(final String usernameParameter) {
        this.usernameParameter = usernameParameter;
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
        logger.warn("Throttling submission from {}.  More than {} failed login attempts within {} seconds. "
                + "Authentication attempt exceeds the failure threshold {}",
                request.getRemoteAddr(), this.failureThreshold, this.failureRangeInSeconds,
                this.failureThreshold);
    }

    /**
     * Record submission failure.
     *
     * @param request the request
     */
    protected abstract void recordSubmissionFailure(HttpServletRequest request);

    /**
     * Determine whether threshold has been exceeded.
     *
     * @param request the request
     * @return true, if successful
     */
    protected abstract boolean exceedsThreshold(HttpServletRequest request);


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("failureThreshold", this.failureThreshold)
                .append("failureRangeInSeconds", this.failureRangeInSeconds)
                .append("usernameParameter", this.usernameParameter)
                .append("thresholdRate", this.thresholdRate)
                .toString();
    }
}
