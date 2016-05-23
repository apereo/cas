package org.jasig.cas.ticket.registry;

import org.jasig.cas.ticket.Ticket;

import java.util.Collection;

/**
 * Interface for a ticket registry that allows retrieving tickets from it in batches.
 *
 * @author Ahsan Rabbani
 */
public interface BatchableTicketRegistry extends TicketRegistry {

    /**
     * Retrieve a batch of TicketGrantingTickets starting from the given offset. Ordering is determined by the
     * implementing class.
     *
     * @param offset the number of rows to skip from the start of the result set
     * @param batchSize the number of tickets to retrieve in the batch. The actual number returned could be less.
     * @return collection of tickets currently stored in the registry. Tickets might or might not be valid i.e. expired.
     */
    Collection<Ticket> getTicketGrantingTicketBatch(int offset, int batchSize);

    /**
     * Retrieve a batch of ServiceTickets starting from the given offset. Ordering is determined by the implementing
     * class.
     *
     * @param offset the number of rows to skip from the start of the result set
     * @param batchSize the number of tickets to retrieve in the batch. The actual number returned could be less.
     * @return collection of tickets currently stored in the registry. Tickets might or might not be valid i.e. expired.
     */
    Collection<Ticket> getServiceTicketBatch(int offset, int batchSize);

}
