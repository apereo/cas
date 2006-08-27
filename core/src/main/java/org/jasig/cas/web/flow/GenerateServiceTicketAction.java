/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.Assert;
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
public final class GenerateServiceTicketAction extends AbstractLoginAction {

    private CentralAuthenticationService centralAuthenticationService;

    protected Event doExecute(final RequestContext context) {
        final Service service = WebUtils.getService(getArgumentExtractors(), WebUtils.getHttpServletRequest(context));
        final String ticketGrantingTicketFromRequest = WebUtils.getTicketGrantingTicketFromRequestScope(context);

        try {
            final String serviceTicketId = this.centralAuthenticationService
                .grantServiceTicket(ticketGrantingTicketFromRequest != null
                    ? ticketGrantingTicketFromRequest
                    : extractTicketGrantingTicketFromCookie(context),
                    service);
            WebUtils.putServiceTicketInRequestScope(context,
                serviceTicketId);
            return success();
        } catch (final TicketException e) {
            if (isGatewayPresent(context)) {
                return result("gateway");
            }
        }

        return error();
    }

    public void setCentralAuthenticationService(
        final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    protected void initActionInternal() throws Exception {
        Assert.notNull(this.centralAuthenticationService,
            "centralAuthenticationService cannot be null.");
    }
}
