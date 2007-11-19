/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging.events;

import org.jasig.cas.event.TicketEvent;
import org.jasig.cas.logging.ClientInfo;
import org.jasig.cas.logging.LogRequest;
import org.jasig.cas.logging.LoggingManager;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.springframework.context.ApplicationEvent;

/**
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public final class TicketLoggingEventHandler extends AbstractLoggingEventHandler {
    
    public TicketLoggingEventHandler(final LoggingManager loggingManager) {
        super(loggingManager);
    }
    
    protected LogRequest constructLogRequest(final ApplicationEvent event,
        final ClientInfo clientInfo) {
        final TicketEvent ticketEvent = (TicketEvent) event;
        final Ticket ticket = ticketEvent.getTicket();
        final TicketGrantingTicket ticketGrantingTicket = (TicketGrantingTicket) (ticket instanceof TicketGrantingTicket ? ticket : ticket.getGrantingTicket());
        
        final String principal = ticketGrantingTicket != null ? ticketGrantingTicket.getAuthentication().getPrincipal().getId() : null;

        final String service = ticket instanceof ServiceTicket ? ((ServiceTicket) ticket).getService().getId() : null;
        final String eventType = ticketEvent.getTicketEventType().name();
        
        return new LogRequest(clientInfo, principal, service, eventType);
    }

    public boolean supports(final ApplicationEvent event) {
        return event instanceof TicketEvent;
    }
}
