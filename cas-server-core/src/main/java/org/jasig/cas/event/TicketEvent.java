/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.event;

import org.jasig.cas.ticket.Ticket;
import org.springframework.util.Assert;

/**
 * Event representing an action taken on a ticket including the creation,
 * validation, destruction of a Ticket.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class TicketEvent extends AbstractEvent {
    
    public static enum TicketEventType {CREATE_TICKET_GRANTING_TICKET, CREATE_SERVICE_TICKET, DESTROY_TICKET_GRANTING_TICKET, VALIDATE_SERVICE_TICKET_SUCCESS, VALIDATE_SERVICE_TICKET_FAILURE, CREATE_PROXY_GRANTING_TICKET, CREATE_PROXY_TICKET}

    /** Unique Serializable Id. */
    private static final long serialVersionUID = 3904682686680347187L;

    /** The TicketEventType for this event. */
    private final TicketEventType ticketEventType;

    /**
     * Constructs a TicketEvent with the Ticket as the source.
     * 
     * @param ticket the source Ticket
     * @param ticketEventType the event type.
     */
    public TicketEvent(final Ticket ticket,
        final TicketEventType ticketEventType) {
        this(ticket, ticketEventType, null);
    }

    /**
     * Constructs a TicketEvent with either the ticketId or the ticket as the
     * source object (depending on whether a Ticket is provided).
     * 
     * @param ticket The ticket the event is for.
     * @param ticketEventType the event type.
     * @param ticketId the id of the ticket.
     * @throws IllegalArgumentException if the EventType is null.
     * @throws IllegalArgumentException if the ticketId and the ticket are null.
     */
    private TicketEvent(final Ticket ticket,
        final TicketEventType ticketEventType, final String ticketId) {
        super((ticket == null) ? (Object) ticketId : ticket);

        Assert.notNull(ticketEventType, "ticketEventType cannot be null");
        Assert.isTrue(ticketId != null || ticket != null,
            "ticketId or ticket is required");

        this.ticketEventType = ticketEventType;
    }

    /**
     * Method to retrieve the Ticket.
     * 
     * @return the ticket, or null if we have no ticket.
     */
    public final Ticket getTicket() {
        return (Ticket) getSource();
    }

    /**
     * Method to retrieve the TicketEventType.
     * 
     * @return the event type.
     */
    public final TicketEventType getTicketEventType() {
        return this.ticketEventType;
    }
}
