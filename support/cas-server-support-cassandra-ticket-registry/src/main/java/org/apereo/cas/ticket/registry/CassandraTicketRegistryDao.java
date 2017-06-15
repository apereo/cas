package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;

import java.util.stream.Stream;

/**
 * @author David Rodriguez
 *
 * @since 5.2.0
 */
public interface CassandraTicketRegistryDao {

    /**
     * Remove a ticketGrantingTicket.
     *
     * @param id ticket's id to be removed
     *
     * @return if ticket was removed
     */
    boolean deleteTicketGrantingTicket(String id);

    /**
     * Return a Stream as there are more operations to do.
     *
     * @return {@link Stream}
     */
    Stream<Ticket> getExpiredTgts();
}
