package org.apereo.cas;

import org.apereo.cas.ticket.Ticket;

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
 * @since 5.2.0
 */
public interface TicketSerializer<T> {

    /**
     * Serialize ticket to the specific type T.
     *
     * @param ticket ticket to be serialized.
     *
     * @return ticket serialized as the specific type T
     */
    T serialize(Ticket ticket);

    /**
     * Deserialize ticketGrantingTicket from the specific type T.
     *
     * @param ticket ticket to be deserialized from.
     * @param ticketClass specific ticket call implementation
     *
     * @return ticket deserialized
     */
    Ticket deserialize(T ticket, Class<? extends Ticket> ticketClass);
}
