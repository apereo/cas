/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.web.flow.util.ContextUtils;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Action that handles the TicketGrantingTicket creation and destruction. If the
 * action is given a TicketGrantingTicket and one also already exists, the old
 * one is destroyed and replaced with the new one. This action always returns
 * "success".
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public final class SendTicketGrantingTicketAction extends
    AbstractCasLoginAction {

    protected Event doExecuteInternal(final RequestContext context,
        final String ticketGrantingTicketId, final String service,
        final boolean gateway, final boolean renew, final boolean warn) {
        final HttpServletResponse response = ContextUtils
            .getHttpServletResponse(context);
        final String ticketGrantingTicketFromRequest = (String) ContextUtils
            .getAttribute(context, REQUEST_ATTRIBUTE_TICKET_GRANTING_TICKET);

        if (ticketGrantingTicketFromRequest == null) {
            return success();
        }

        if (ticketGrantingTicketId != null) {
            getCentralAuthenticationService().destroyTicketGrantingTicket(
                ticketGrantingTicketId);
        }

        getTicketGrantingTicketCookieGenerator().addCookie(response,
            ticketGrantingTicketFromRequest);

        return success();
    }
}
