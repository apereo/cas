package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;

/**
 * This is {@link TicketRegistryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface TicketRegistryCleaner {

    /**
     * The bean name.
     */
    String BEAN_NAME = "ticketRegistryCleaner";

    /**
     * Clean the ticket registry by collecting
     * tickets in the storage unit that may be expired.
     *
     * @return the int
     */
    default int clean() {
        return 0;
    }

    /**
     * Cleans up after an already-expired ticket, by running the necessary processes
     * such as logout notifications and more.
     *
     * @param ticket the ticket
     * @return the number of tickets that were cleaned up during the process.
     */
    default int cleanTicket(final Ticket ticket) {
        return 0;
    }
}
