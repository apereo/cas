package org.jasig.cas.ticket;

import org.jasig.cas.authentication.Authentication;

import java.time.ZonedDateTime;

/**
 * @author Scott Battaglia

 * @since 3.0.0
 */
public interface TicketState {

    /**
     * Returns the number of times a ticket was used.
     *
     * @return the number of times the ticket was used.
     */
    int getCountOfUses();

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
     * Get the time the ticket was created.
     *
     * @return the creation time of the ticket.
     */
    ZonedDateTime getCreationTime();

    /**
     * Authentication information from the ticket. This may be null.
     *
     * @return the authentication information.
     */
    Authentication getAuthentication();
}
