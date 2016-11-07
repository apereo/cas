package org.apereo.cas;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;

public interface TicketSerializer {

    Object serialize(Ticket ticket);

    TicketGrantingTicket deserializeTGT(String ticket);

    ServiceTicket deserializeST(String ticket);
}
