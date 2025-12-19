package org.apereo.cas.ticket.registry.pubsub;

import module java.base;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;

/**
 * This is {@link QueueableTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface QueueableTicketRegistry extends TicketRegistry {

    /**
     * Add ticket to queue.
     *
     * @param ticket the ticket
     * @throws Exception the exception
     */
    void addTicketToQueue(Ticket ticket) throws Exception;

    /**
     * Update ticket in queue.
     *
     * @param ticket the ticket
     * @return the ticket
     * @throws Exception the exception
     */
    Ticket updateTicketInQueue(Ticket ticket) throws Exception;

    /**
     * Delete ticket from queue.
     *
     * @param ticketId the ticket id
     * @return the long
     */
    long deleteTicketFromQueue(String ticketId);

    /**
     * Delete all from queue.
     *
     * @return the long
     */
    long deleteAllFromQueue();
}
