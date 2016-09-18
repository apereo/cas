package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;

/**
 * The {@link TicketGrantingTicketFactory} is responsible for
 * creating instances of {@link TicketGrantingTicket}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface TicketGrantingTicketFactory extends TicketFactory {

    /**
     * Create the ticket object.
     *
     * @param <T>            the type parameter
     * @param authentication the authentication
     * @return the t
     */
    <T extends TicketGrantingTicket> T create(Authentication authentication);
}
