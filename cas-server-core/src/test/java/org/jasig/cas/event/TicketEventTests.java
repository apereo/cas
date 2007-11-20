/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.event;

import org.jasig.cas.TestUtils;
import org.jasig.cas.event.TicketEvent.TicketEventType;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class TicketEventTests extends TestCase {

    private TicketGrantingTicket tgt = new TicketGrantingTicketImpl("test1",
        TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

    private TicketEvent ticketEvent1 = new TicketEvent(this.tgt,
        TicketEventType.CREATE_TICKET_GRANTING_TICKET);

    public void testGetTicketId() {
        assertEquals("test1", this.ticketEvent1.getTicket().getId());
    }

    public void testGetTicket() {
        assertEquals(this.tgt, this.ticketEvent1.getTicket());
    }

    public void testGetTicketEventType() {
        assertEquals(TicketEventType.CREATE_TICKET_GRANTING_TICKET,
            this.ticketEvent1.getTicketEventType());
    }

    public void testGetTicketEventTypeAsString() {
        assertEquals("CREATE_TICKET_GRANTING_TICKET", this.ticketEvent1
            .getTicketEventType().toString());
    }
    
    public void testGetTicketEventTypeToString() {
        assertEquals("CREATE_TICKET_GRANTING_TICKET", this.ticketEvent1
            .getTicketEventType().toString());
    }
}
