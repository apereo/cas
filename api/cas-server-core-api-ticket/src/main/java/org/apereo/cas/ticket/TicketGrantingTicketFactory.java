package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;

/**
 * The {@link TicketGrantingTicketFactory} is responsible for
 * creating instances of {@link TicketGrantingTicket}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface TicketGrantingTicketFactory<T extends TicketGrantingTicket> extends TicketFactory {

    /**
     * Create the ticket object.
     *
     * @param authentication the authentication
     * @param service        the service
     * @return the t
     * @throws Throwable the throwable
     */
    T create(Authentication authentication, Service service) throws Throwable;
}
