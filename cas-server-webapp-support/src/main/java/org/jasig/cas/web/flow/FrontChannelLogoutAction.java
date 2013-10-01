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

import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.logout.LogoutRequest;
import org.jasig.cas.logout.LogoutRequestStatus;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Logout action for front SLO : find the next eligible service and perform front logout.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public final class FrontChannelLogoutAction extends AbstractLogoutAction {

    @NotNull
    private final LogoutManager logoutManager;

    /**
     * Build from the logout manager.
     *
     * @param logoutManager a logout manager.
     */
    public FrontChannelLogoutAction(final LogoutManager logoutManager) {
        this.logoutManager = logoutManager;
    }

    @Override
    protected Event doInternalExecute(final HttpServletRequest request, final HttpServletResponse response,
            final RequestContext context) throws Exception {

        final List<LogoutRequest> logoutRequests = WebUtils.getLogoutRequests(context);
        final Integer startIndex = getLogoutIndex(context);
        if (logoutRequests != null && startIndex != null) {
            for (int i = startIndex; i < logoutRequests.size(); i++) {
                final LogoutRequest logoutRequest = logoutRequests.get(i);
                if (logoutRequest.getStatus() == LogoutRequestStatus.NOT_ATTEMPTED) {
                    // assume it has been successful
                    logoutRequest.setStatus(LogoutRequestStatus.SUCCESS);

                    // save updated index
                    putLogoutIndex(context, i + 1);

                    // redirect to application with SAML logout message
                    final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(logoutRequest.getService().getId());
                    builder.queryParam("SAMLRequest",
                            URLEncoder.encode(logoutManager.createFrontChannelLogoutMessage(logoutRequest), "UTF-8"));
                    return result(REDIRECT_APP_EVENT, "logoutUrl", builder.build().toUriString());
                }
            }
        }

        // no new service with front-channel logout -> finish logout
        return new Event(this, FINISH_EVENT);
    }

    public LogoutManager getLogoutManager() {
        return logoutManager;
    }
}
