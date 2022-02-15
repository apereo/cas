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
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface Ticket extends Serializable, Comparable<Ticket> {

    /**
     * Method to retrieve the id.
     *
     * @return the id
     */
    String getId();

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
     * Gets prefix.
     *
     * @return the prefix
     */
    String getPrefix();

    /**
     * Determines if the ticket is expired. Most common implementations might
     * collaborate with <i>ExpirationPolicy</i> strategy.
     *
     * @return true, if the ticket is expired
     * @see ExpirationPolicy
     */
    boolean isExpired();

    /**
     * Get expiration policy associated with ticket.
     *
     * @return the expiration policy
     */
    ExpirationPolicy getExpirationPolicy();

    /**
     * Mark a ticket as expired.
     */
    void markTicketExpired();

    /**
     * Returns the last time the ticket was used.
     *
     * @return the last time the ticket was used.
     */
    ZonedDateTime getLastTimeUsed();

    /**
     * Get the second to last time used.
     *
     * @return the previous time used.
     */
    ZonedDateTime getPreviousTimeUsed();

    /**
     * Records the <i>previous</i> last time this ticket was used as well as
     * the last usage time. The ticket usage count is also incremented.
     * <p>Tickets themselves are solely responsible to maintain their state. The
     * determination of ticket usage is left up to the implementation and
     * the specific ticket type.
     *
     * @see ExpirationPolicy
     * @since 5.0.0
     */
    void update();
}
