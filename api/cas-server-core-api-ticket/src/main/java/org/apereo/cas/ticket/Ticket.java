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
    default ZonedDateTime getCreationTime() {
        return null;
    }

    /**
     * Sets creation time.
     *
     * @param creationTime the creation time
     */
    default void setCreationTime(final ZonedDateTime creationTime) {}

    /**
     * Gets count of uses.
     *
     * @return the number of times this ticket was used.
     */
    default int getCountOfUses() {
        return 0;
    }

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
    default boolean isExpired() {
        return false;
    }

    /**
     * Indicate whether ticket is a compact (usually a JWT) ticket.
     *
     * @return true/false
     */
    default boolean isStateless() {
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
     * Records the <i>previous</i> last time this ticket was used as well as
     * the last usage time. The ticket usage count is also incremented.
     * <p>Tickets themselves are solely responsible to maintain their state. The
     * determination of ticket usage is left up to the implementation and
     * the specific ticket type.
     *
     * @see ExpirationPolicy
     * @since 5.0.0
     */
    default void update() {}

    /**
     * Mark this ticket as compact and stateless. A stateless ticket usually is self contained, such as a JWT.
     */
    default Ticket markTicketStateless() {
        return this;
    }
}
