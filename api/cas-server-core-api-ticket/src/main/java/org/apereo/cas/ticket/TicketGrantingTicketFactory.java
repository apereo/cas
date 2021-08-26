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
public interface TicketGrantingTicketFactory extends TicketFactory {

    /**
     * Create the ticket object.
     *
     * @param <T>            the type parameter
     * @param authentication the authentication
     * @param service        the service
     * @param clazz          the clazz
     * @return the t
     */
    <T extends TicketGrantingTicket> T create(Authentication authentication, Service service, Class<T> clazz);
}
