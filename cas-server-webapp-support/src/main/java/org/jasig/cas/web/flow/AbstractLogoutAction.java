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
package org.jasig.cas.web.flow;

import org.jasig.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract logout action, which prevents caching on logout.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public abstract class AbstractLogoutAction extends AbstractAction {

    /** A constant for the logout index in web flow. */
    public static final String LOGOUT_INDEX = "logoutIndex";

    /** The finish event in webflow. */
    public static final String FINISH_EVENT = "finish";

    /** The front event in webflow. */
    public static final String FRONT_EVENT = "front";

    /** The redirect to app event in webflow. */
    public static final String REDIRECT_APP_EVENT = "redirectApp";

    @Override
    protected final Event doExecute(final RequestContext context) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);

        preventCaching(response);

        return doInternalExecute(request, response, context);
    }

    /**
     * Execute the logout action after invalidating the cache.
     *
     * @param request the HTTP request.
     * @param response the HTTP response.
     * @param context the webflow context.
     * @return the event triggered by this actions.
     * @throws Exception exception returned by this action.
     */
    protected abstract Event doInternalExecute(HttpServletRequest request, HttpServletResponse response,
            RequestContext context) throws Exception;

    /**
     * Prevent caching by adding the appropriate headers.
     * Copied from the <code>preventCaching</code> method in the
     * {@link org.springframework.web.servlet.support.WebContentGenerator} class.
     *
     * @param response the HTTP response.
     */
    protected final void preventCaching(final HttpServletResponse response) {
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 1L);
        response.setHeader("Cache-Control", "no-cache");
        response.addHeader("Cache-Control", "no-store");
    }

    /**
     * Put logout index into flow scope.
     *
     * @param context the context
     * @param index the index
     */
    protected final void putLogoutIndex(final RequestContext context, final int index) {
        context.getFlowScope().put(LOGOUT_INDEX, index);
    }

    /**
     * Gets the logout index from the flow scope.
     *
     * @param context the context
     * @return the logout index
     */
    protected final int getLogoutIndex(final RequestContext context) {
        final Object value = context.getFlowScope().get(LOGOUT_INDEX);
        return value == null ? 0 : (Integer) value;
    }
}
