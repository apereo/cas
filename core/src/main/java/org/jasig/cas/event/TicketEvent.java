/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.event;

import java.io.Serializable;

import org.jasig.cas.ticket.Ticket;

/**
 * Event representing an action taken on a ticket including
 * the creation, validation, destruction of a Ticket.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class TicketEvent extends AbstractEvent {
    
    /** TicketEvent of type Create Ticket Granting Ticket. */
    public static final TicketEventType CREATE_TICKET_GRANTING_TICKET = new TicketEventType("CREATE_TICKET_GRANTING_TICKET");

    /** TicketEvent of type Create Service Ticket. */
    public static final TicketEventType CREATE_SERVCE_TICKET = new TicketEventType("CREATE_SERVICE_TICKET");
    
    /** TicketEvent of type Destroy Ticket Granting Ticket. */
    public static final TicketEventType DESTROY_TICKET_GRANTING_TICKET = new TicketEventType("DESTROY_TICKET_GRANTING_TICKET");
    
    /** TicketEvent of type Validate Service Ticket. */
    public static final TicketEventType VALIDATE_SERVICE_TICKET = new TicketEventType("VALIDATE_SERVICE_TICKET");

    /** Unique Serializable Id. */
    private static final long serialVersionUID = 3904682686680347187L;

    /** The TicketEventType for this event. */
    private final TicketEventType ticketEventType;
    
    /** The String id of the Ticket for this event. */
    private final String ticketId;
    
    public TicketEvent(final Ticket ticket, final TicketEventType ticketEventType) {
        this(ticket, ticketEventType, null);
    }
    
    public TicketEvent(final TicketEventType ticketEventType, final String ticketId) {
        this(null, ticketEventType, ticketId);
    }
    
    private TicketEvent(final Ticket ticket, final TicketEventType ticketEventType, final String ticketId) {
        super((ticket == null) ? (Object) ticketId : ticket);
        
        if (ticketEventType == null) {
            throw new IllegalStateException("ticketEventType cannot be null on " + this.getClass().getName());
        }
        
        if (ticketId == null && ticket == null) {
            throw new IllegalStateException("Either ticketId or Ticket need to be provided.");
        }
        
        if (ticket != null) {
            this.ticketId = ticket.getId();
        } else {
            this.ticketId = ticketId;    
        }
        
        this.ticketEventType = ticketEventType;
    }
    /**
     * Method to retrieve the Id of the Ticket.
     * @return the id of the ticket.
     */
    public String getTicketId() {
        return this.ticketId; 
    }
    
    /**
     * Method to retrieve the Ticket.
     * @return the ticket, or null if we have no ticket.
     */
    public Ticket getTicket() {
        return (getSource() instanceof Ticket) ? (Ticket) getSource() : null;
    }
    
    /** Method to retrieve the TicketEventType
     * 
     * @return the event type.
     */
    public TicketEventType getTicketEventType() {
        return this.ticketEventType;
    }
    
    protected final static class TicketEventType implements Serializable {

        private static final long serialVersionUID = 3258689897039671865L;

        private final String name;

        protected TicketEventType(String name) {
            this.name = name; 
        }
        
        public String getEventTypeAsString() {
            return this.name;
        }
    }
}
