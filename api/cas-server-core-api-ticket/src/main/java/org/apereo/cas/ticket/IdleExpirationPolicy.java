package org.apereo.cas.ticket;

import module java.base;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Strategy that determines if the ticket is expired. Implementations of the
 * Expiration Policy define their own rules on what they consider an expired
 * Ticket to be.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface IdleExpirationPolicy extends ExpirationPolicy {

    /**
     * Describes the idle time duration for the item.
     *
     * @return idle time in seconds. A zero value indicates the time duration is not supported or is inactive. Unit of measure is defined by the implementation.
     */
    default Long getTimeToIdle() {
        return 0L;
    }

    /**
     * Gets idle expiration time for this ticket.
     * The ticket will expire at the calculated time if idle.
     *
     * @param ticketState the ticket state
     * @return the idle expiration time
     */
    default ZonedDateTime getIdleExpirationTime(final Ticket ticketState) {
        return null;
    }
}
