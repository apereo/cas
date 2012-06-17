/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.core.collection.AttributeMap;

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
    
    private static final String SUCCESSFUL_AUTHENTICATION_EVENT = "success";

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

        RequestContext context = (RequestContext) request.getAttribute("flowRequestContext");
        
        if (context == null || context.getCurrentEvent() == null) {
            return;
        }
        
        // User successfully authenticated
        if (SUCCESSFUL_AUTHENTICATION_EVENT.equals(context.getCurrentEvent().getId())) {
            return;
        }

        // User submitted invalid credentials, so we update the invalid login count
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
