package org.apereo.cas.ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Interface for the generic concept of a ticket.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface Ticket extends Serializable, Comparable<Ticket> {

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
     * @see ExpirationPolicy
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
    ZonedDateTime getCreationTime();

    /**
     * Gets count of uses.
     *
     * @return the number of times this ticket was used.
     */
    int getCountOfUses();

    /**
     * Get expiration policy associated with ticket.
     *
     * @return the expiration policy
     */
    ExpirationPolicy getExpirationPolicy();

    /**
     * Gets prefix.
     *
     * @return the prefix
     */
    String getPrefix();
}
