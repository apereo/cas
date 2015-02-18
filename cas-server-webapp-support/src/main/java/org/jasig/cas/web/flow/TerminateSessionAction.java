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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Terminates the CAS SSO session by destroying all SSO state data (i.e. TGT, cookies).
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class TerminateSessionAction {

    /** Webflow event helper component. */
    private final EventFactorySupport eventFactorySupport = new EventFactorySupport();

    /** The CORE to which we delegate for all CAS functionality. */
    @NotNull
    private final CentralAuthenticationService centralAuthenticationService;

    /** CookieGenerator for TGT Cookie. */
    @NotNull
    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    /** CookieGenerator for Warn Cookie. */
    @NotNull
    private final CookieRetrievingCookieGenerator warnCookieGenerator;

    /**
     * Creates a new instance with the given parameters.
     * @param cas Core business logic object.
     * @param tgtCookieGenerator TGT cookie generator.
     * @param warnCookieGenerator Warn cookie generator.
     */
    public TerminateSessionAction(
            final CentralAuthenticationService cas,
            final CookieRetrievingCookieGenerator tgtCookieGenerator,
            final CookieRetrievingCookieGenerator warnCookieGenerator) {
        this.centralAuthenticationService = cas;
        this.ticketGrantingTicketCookieGenerator = tgtCookieGenerator;
        this.warnCookieGenerator = warnCookieGenerator;
    }

    /**
     * Terminates the CAS SSO session by destroying the TGT (if any) and removing cookies related to the SSO session.
     *
     * @param context Request context.
     *
     * @return "success"
     */
    public Event terminate(final RequestContext context) {
        // in login's webflow : we can get the value from context as it has already been stored
        String tgtId = WebUtils.getTicketGrantingTicketId(context);
        // for logout, we need to get the cookie's value
        if (tgtId == null) {
            final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
            tgtId = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        }
        if (tgtId != null) {
            WebUtils.putLogoutRequests(context, this.centralAuthenticationService.destroyTicketGrantingTicket(tgtId));
        }
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        this.ticketGrantingTicketCookieGenerator.removeCookie(response);
        this.warnCookieGenerator.removeCookie(response);
        return this.eventFactorySupport.success(this);
    }
}
