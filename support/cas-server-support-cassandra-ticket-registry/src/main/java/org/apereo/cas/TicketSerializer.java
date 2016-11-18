package org.apereo.cas;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;

/**
 * Abstraction layer for ticket serialization methods
 *
 * Currently used by {@link org.apereo.cas.dao.CassandraDao}
 * so {@param <T>} should be something that can be written
 * to Cassandra like {@link String}
 */
public interface TicketSerializer<T> {

    T serializeTGT(Ticket ticket);

    T serializeST(Ticket ticket);

    TicketGrantingTicket deserializeTGT(T ticket);

    ServiceTicket deserializeST(T ticket);
}
