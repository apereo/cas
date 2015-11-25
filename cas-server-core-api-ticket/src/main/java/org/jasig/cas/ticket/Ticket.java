package org.jasig.cas.ticket;

import java.io.Serializable;

/**
 * Interface for the generic concept of a ticket.
 *
 * @author Scott Battaglia

 * @since 3.0.0
 */
public interface Ticket extends Serializable {

    /**
     * Method to retrieve the id.
     *
     * @return the id
     */
    String getId();

    /**
     * Determines if the ticket is expired. Most common implementations might
     * collaborate with <i>ExpirationPolicy </i> strategy.
     *
     * @return true, if the ticket is expired
     * @see org.jasig.cas.ticket.ExpirationPolicy
     */
    boolean isExpired();

    /**
     * Method to retrieve the TicketGrantingTicket that granted this ticket.
     *
     * @return the ticket or null if it has no parent
     */
    TicketGrantingTicket getGrantingTicket();

    /**
     * Method to return the time the Ticket was created.
     *
     * @return the time the ticket was created.
     */
    long getCreationTime();

    /**
     * @return the number of times this ticket was used.
     */
    int getCountOfUses();
}
