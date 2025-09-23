package org.apereo.cas.ticket;

import java.io.Serializable;

/**
 * This is {@link StatelessTicket}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public interface StatelessTicket extends Serializable {
    /**
     * Indicate whether ticket is a compact (usually a JWT) ticket.
     *
     * @return true/false
     */
    default boolean isStateless() {
        return false;
    }

    /**
     * Mark this ticket as compact and stateless. A stateless ticket usually is self contained, such as a JWT.
     */
    default void markTicketStateless() {
    }
}
