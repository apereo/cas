package org.apereo.cas;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;

public interface TicketSerializer<T> {

    T serializeTGT(Ticket ticket);

    T serializeST(Ticket ticket);

    TicketGrantingTicket deserializeTGT(T ticket);

    ServiceTicket deserializeST(T ticket);
}
