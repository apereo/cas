/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.jasig.cas.web.util.WebUtils;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Action to generate a service ticket for a given Ticket Granting Ticket and
 * Service.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public final class GenerateServiceTicketAction extends AbstractCasLoginAction {

    protected Event doExecuteInternal(final RequestContext context,
        final String ticketGrantingTicketId, final String service,
        final boolean gateway, final boolean renew, final boolean warn) {
        final String ticketGrantingTicketFromRequest = (String) ContextUtils
            .getAttribute(context, REQUEST_ATTRIBUTE_TICKET_GRANTING_TICKET);

        try {
            final String serviceTicketId = getCentralAuthenticationService()
                .grantServiceTicket(
                    ticketGrantingTicketFromRequest != null
                        ? ticketGrantingTicketFromRequest
                        : ticketGrantingTicketId, new SimpleService(WebUtils.stripJsessionFromUrl(service)));
            ContextUtils.addAttribute(context, WebConstants.TICKET,
                serviceTicketId);
            return success();
        } catch (final TicketException e) {
            if (gateway) {
                return gateway();
            }
        }

        return error();
    }
}
