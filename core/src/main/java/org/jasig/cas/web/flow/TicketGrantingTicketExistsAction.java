/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Action to determine if there is a TicketGrantingTicket or not. One of two
 * flows will start depending on if there is a TicketGrantingTicket or not. This
 * is so that subsequent actions do not need to re-check if there is a Ticket or
 * not.
 * <p>
 * If a Ticket exists, a <code>ticketGrantingTicketExists</code> event will be
 * published. Otherwise, a <code>noTicketGrantingTicketExists</code> event is
 * published.
 * <p>
 * In the default logon flow, this is the first action executed.
 * 
 * @author Scott
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public final class TicketGrantingTicketExistsAction extends AbstractLoginAction {

    /** Event string to denote a TicketGrantingTicket exists. */
    private static final String EVENT_TICKET_GRANTING_TICKET_EXISTS = "ticketGrantingTicketExists";

    /** Event string to denote a TicketGrantingTicket does not exist. */
    private static final String EVENT_NO_TICKET_GRANTING_TICKET_EXISTS = "noTicketGrantingTicketExists";

    protected Event doExecuteInternal(final RequestContext request,
        final String ticketGrantingTicketId, final String service,
        final boolean gateway, final boolean renew, final boolean warn) {

        return ticketGrantingTicketId == null ? noTicketGrantingTicketExists()
            : ticketGrantingTicketExists();
    }

    /**
     * Method to create a noTicketGrantingTicketExists event.
     * 
     * @return an Event symbolized by the text "noTicketGrantingTicketExists".
     */
    private Event noTicketGrantingTicketExists() {
        return result(EVENT_NO_TICKET_GRANTING_TICKET_EXISTS);
    }

    /**
     * Method to create a ticketGrantingTicketExists event.
     * 
     * @return an Event symbolized by the text "ticketGrantingTicketExists".
     */
    private Event ticketGrantingTicketExists() {
        return result(EVENT_TICKET_GRANTING_TICKET_EXISTS);
    }
}
