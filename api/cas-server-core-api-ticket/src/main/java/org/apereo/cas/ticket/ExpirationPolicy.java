package org.apereo.cas.ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Strategy that determines if the ticket is expired. Implementations of the
 * Expiration Policy define their own rules on what they consider an expired
 * Ticket to be.
 *
 * @author Scott Battaglia
 * @see Ticket
 * @since 3.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface ExpirationPolicy extends Serializable {

    /**
     * Method to determine if a Ticket has expired or not, based on the policy.
     *
     * @param ticketState The snapshot of the current ticket state
     * @return true if the ticket is expired, false otherwise.
     */
    boolean isExpired(TicketState ticketState);

    /**
     * Describes the time duration where this policy should consider the item alive.
     * Once this time passes, the item is considered expired and dead.
     *
     * @return time to live in seconds. A zero value indicates the time duration is not supported or is inactive.
     */
    Long getTimeToLive();

    /**
     * Describes the idle time duration for the item.
     *
     * @return idle time in seconds. A zero value indicates the time duration is not supported or is inactive. Unit of measure is defined by the implementation.
     */
    Long getTimeToIdle();

    /**
     * Gets name of this expiration policy.
     *
     * @return the name
     */
    String getName();
}
