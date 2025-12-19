package org.apereo.cas.ticket;

import module java.base;

/**
 * This is {@link ExpirableTicket}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public interface ExpirableTicket extends Serializable {

    /**
     * Determines if the ticket is expired. Most common implementations might
     * collaborate with <i>ExpirationPolicy</i> strategy.
     *
     * @return true, if the ticket is expired
     * @see ExpirationPolicy
     */
    default boolean isExpired() {
        return false;
    }

    /**
     * Get expiration policy associated with ticket.
     *
     * @return the expiration policy
     */
    default ExpirationPolicy getExpirationPolicy() {
        return null;
    }

    /**
     * Sets expiration policy.
     *
     * @param expirationPolicy the expiration policy
     */
    default void setExpirationPolicy(final ExpirationPolicy expirationPolicy) {}

    /**
     * Mark a ticket as expired.
     */
    default void markTicketExpired() {}

    /**
     * Returns the last time the ticket was used.
     *
     * @return the last time the ticket was used.
     */
    default ZonedDateTime getLastTimeUsed() {
        return null;
    }

    /**
     * Get the second to last time used.
     *
     * @return the previous time used.
     */
    default ZonedDateTime getPreviousTimeUsed() {
        return null;
    }

    /**
     * Method to return the time the Ticket was created.
     *
     * @return the time the ticket was created.
     */
    default ZonedDateTime getCreationTime() {
        return null;
    }

    /**
     * Sets creation time.
     *
     * @param creationTime the creation time
     */
    default void setCreationTime(final ZonedDateTime creationTime) {}
}
