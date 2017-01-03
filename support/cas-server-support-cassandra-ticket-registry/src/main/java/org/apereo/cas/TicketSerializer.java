package org.apereo.cas;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;

/**
 * This is {@link TicketSerializer}.
 *
 * Abstraction layer for ticket serialization methods
 *
 * @author David Rodriguez
 *
 * @param <T> should be something that can be written
 * to Cassandra like {@link String}
 *
 * @since 5.1.0
 */
public interface TicketSerializer<T> {

    /**
     * Serialize ticketGrantingTicket to the specific type T.
     *
     * @param ticket ticket to be serialized.
     *
     * @return ticket serialized as the specific type T
     */
    T serializeTGT(Ticket ticket);

    /**
     * Serialize serviceTicket to the specific type T.
     *
     * @param ticket ticket to be serialized.
     *
     * @return ticket serialized as the specific type T
     */
    T serializeST(Ticket ticket);

    /**
     * Deserialize ticketGrantingTicket from the specific type T.
     *
     * @param ticket ticket to be deserialized from.
     *
     * @return ticketGrantingTicket deserialized
     */
    TicketGrantingTicket deserializeTGT(T ticket);

    /**
     * Deserialize serviceTicket from the specific type T.
     *
     * @param ticket ticket to be deserialized from.
     *
     * @return serviceTicket deserialized
     */
    ServiceTicket deserializeST(T ticket);
}
