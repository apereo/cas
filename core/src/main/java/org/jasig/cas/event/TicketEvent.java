/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.event;

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
    
    public static final TicketEventType CREATE_TICKET_GRANTING_TICKET = new TicketEventType("CREATE_TICKET_GRANTING_TICKET");
    
    public static final TicketEventType CREATE_SERVCE_TICKET = new TicketEventType("CREATE_SERVICE_TICKET");
    
    public static final TicketEventType DESTROY_TICKET_GRANTING_TICKET = new TicketEventType("DESTROY_TICKET_GRANTING_TICKET");
    
    public static final TicketEventType VALIDATE_SERVICE_TICKET = new TicketEventType("VALIDATE_SERVICE_TICKET");

    private static final long serialVersionUID = 3904682686680347187L;

    private final TicketEventType ticketEventType;
    
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
        
        this.ticketEventType = ticketEventType;
        this.ticketId = ticketId;
    }
    
    public String getTicketId() {
        return getTicket() == null ? this.ticketId : getTicket().getId(); 
    }
    
    public Ticket getTicket() {
        return (getSource() instanceof Ticket) ? (Ticket) getSource() : null;
    }
    
    public TicketEventType getTicketEventType() {
        return this.ticketEventType;
    }
    
    protected final static class TicketEventType {
        private final String name;

        protected TicketEventType(String name) {
            this.name = name; 
        }
        
        public String getEventTypeAsString() {
            return this.name;
        }
    }
}
