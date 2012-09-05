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
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;

/**
 * Listener to expire web session as soon as the webflow is ended. The goal is to decrease memory consumption by deleting as soon as
 * possible the web sessions created mainly for login process.
 * 
 * @author Jerome Leleu
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public final class TerminateWebSessionListener extends FlowExecutionListenerAdapter {

    /** Session marker that if present indicates a session that should not be terminated by this component. */
    private static final String DO_NOT_TERMINATE = TerminateWebSessionListener.class + ".DO_NOT_TERMINATE";
    
    private static final Logger logger = LoggerFactory.getLogger(TerminateWebSessionListener.class);

    @Min(0)
    private int timeToDieInSeconds = 2;

    /** URL to service manager Web application. */
    @NotNull
    private String serviceManagerUrl;

    @Override
    public void sessionStarted(final RequestContext context, final FlowSession session) {
        final Service service;
        // Guard against exceptions that arise from attempts to access terminated flow sessions
        try {
            service = WebUtils.getService(context);
        } catch (final IllegalStateException e) {
            logger.debug("Error getting service from flow state.", e);
            return;
        }
        // If the user has requested a ticket for the service manager application
        // then tag the session so it is not terminated.
        if (service != null && service.getId().startsWith(serviceManagerUrl)) {
            final HttpSession webSession = WebUtils.getHttpServletRequest(context).getSession(false);
            if (webSession != null) {
                webSession.setAttribute(DO_NOT_TERMINATE, true);
            }
        }
    }

    @Override
    public void sessionEnded(final RequestContext context, final FlowSession session, final String outcome,
                             final AttributeMap output) {

        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        // get session but don't create it if it doesn't already exist
        final HttpSession webSession = request.getSession(false);

        if (webSession != null && webSession.getAttribute(DO_NOT_TERMINATE) == null) {
            logger.debug("Terminate web session {} in {} seconds", webSession.getId(), this.timeToDieInSeconds);
            // set the web session to die in timeToDieInSeconds
            webSession.setMaxInactiveInterval(this.timeToDieInSeconds);
        }
    }

    public int getTimeToDieInSeconds() {
        return this.timeToDieInSeconds;
    }

    public void setTimeToDieInSeconds(final int timeToDieInSeconds) {
        this.timeToDieInSeconds = timeToDieInSeconds;
    }

    /**
     * Sets the URL to the service manager Web application.
     *
     * @param  url  URL to service manager.
     */
    public void setServiceManagerUrl(final String url) {
        this.serviceManagerUrl = url;
    }
}
