/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event;

import java.io.Serializable;

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

    /** TicketEvent of type Create Ticket Granting Ticket. */
    public static final TicketEventType CREATE_TICKET_GRANTING_TICKET = new TicketEventType(
        "CREATE_TICKET_GRANTING_TICKET");

    /** TicketEvent of type Create Service Ticket. */
    public static final TicketEventType CREATE_SERVICE_TICKET = new TicketEventType(
        "CREATE_SERVICE_TICKET");

    /** TicketEvent of type Destroy Ticket Granting Ticket. */
    public static final TicketEventType DESTROY_TICKET_GRANTING_TICKET = new TicketEventType(
        "DESTROY_TICKET_GRANTING_TICKET");

    /** TicketEvent of type Validate Service Ticket. */
    public static final TicketEventType VALIDATE_SERVICE_TICKET = new TicketEventType(
        "VALIDATE_SERVICE_TICKET");

    /** Unique Serializable Id. */
    private static final long serialVersionUID = 3904682686680347187L;

    /** The TicketEventType for this event. */
    private final TicketEventType ticketEventType;

    /** The String id of the Ticket for this event. */
    private final String ticketId;

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
     * Constructs a TicketEvent without a Ticket using the TicketId as the
     * source object.
     * 
     * @param ticketEventType the type of TicketEvent
     * @param ticketId the identifier for the ticket.
     */
    public TicketEvent(final TicketEventType ticketEventType,
        final String ticketId) {
        this(null, ticketEventType, ticketId);
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
        Assert.isTrue(ticketId != null || ticket != null, "ticketId or ticket is required");

        if (ticket != null) {
            this.ticketId = ticket.getId();
        } else {
            this.ticketId = ticketId;
        }

        this.ticketEventType = ticketEventType;
    }

    /**
     * Method to retrieve the Id of the Ticket.
     * 
     * @return the id of the ticket.
     */
    public final String getTicketId() {
        return this.ticketId;
    }

    /**
     * Method to retrieve the Ticket.
     * 
     * @return the ticket, or null if we have no ticket.
     */
    public final Ticket getTicket() {
        return (getSource() instanceof Ticket) ? (Ticket) getSource() : null;
    }

    /**
     * Method to retrieve the TicketEventType.
     * 
     * @return the event type.
     */
    public final TicketEventType getTicketEventType() {
        return this.ticketEventType;
    }

    /**
     * Enumeration class to denote what ticket event occurred.
     * 
     * @author Scott Battaglia
     * @version $Revision$ $Date$
     * @since 3.0
     */
    public static final class TicketEventType implements Serializable {

        /** Serializable Unique Id. */
        private static final long serialVersionUID = 3258689897039671865L;

        /** The name of the event. */
        private final String name;

        protected TicketEventType(final String name) {
            this.name = name;
        }

        public String getEventTypeAsString() {
            return this.name;
        }
        
        public String toString() {
            return this.name;
        }
    }
}
