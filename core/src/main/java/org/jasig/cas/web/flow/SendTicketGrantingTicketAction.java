/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.CentralAuthenticationService;
import org.springframework.util.Assert;
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
public final class SendTicketGrantingTicketAction extends AbstractLoginAction {

    private CentralAuthenticationService centralAuthenticationService;

    protected Event doExecute(final RequestContext context) {
        final String ticketGrantingTicketId = getCasArgumentExtractor()
            .extractTicketGrantingTicketFromCookie(context);
        final String ticketGrantingTicketFromRequest = getCasArgumentExtractor()
            .getTicketGrantingTicketFrom(context);

        if (ticketGrantingTicketFromRequest == null) {
            return success();
        }

        if (ticketGrantingTicketId != null) {
            this.centralAuthenticationService
                .destroyTicketGrantingTicket(ticketGrantingTicketId);
        }

        getCasArgumentExtractor().putTicketGrantingTicketInCookie(context,
            ticketGrantingTicketFromRequest);

        return success();
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
