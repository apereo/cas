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
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;

/**
 * Listener to expire web session as soon as the webflow is ended. The goal is to decrease memory
 * consumption by deleting as soon as
 * possible the web sessions created mainly for login process.
 *
 * @author Jerome Leleu
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public final class TerminateWebSessionListener extends FlowExecutionListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminateWebSessionListener.class);

    @Min(0)
    private int timeToDieInSeconds = 2;

    @Override
    public void sessionEnded(final RequestContext context, final FlowSession session, final String outcome,
                             final AttributeMap output) {

        if ( session.isRoot() ) {
            final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
            // get session but don't create it if it doesn't already exist
            final HttpSession webSession = request.getSession(false);

            if (webSession != null) {
                LOGGER.debug("Terminate web session {} in {} seconds", webSession.getId(), this.timeToDieInSeconds);
                // set the web session to die in timeToDieInSeconds
                webSession.setMaxInactiveInterval(this.timeToDieInSeconds);
            }
        }
    }

    public int getTimeToDieInSeconds() {
        return this.timeToDieInSeconds;
    }

    public void setTimeToDieInSeconds(final int timeToDieInSeconds) {
        this.timeToDieInSeconds = timeToDieInSeconds;
    }
}
