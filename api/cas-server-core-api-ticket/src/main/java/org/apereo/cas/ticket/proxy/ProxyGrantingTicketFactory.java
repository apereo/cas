package org.apereo.cas.ticket.proxy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketFactory;

/**
 * The {@link ProxyGrantingTicketFactory} is responsible for
 * creating instances of {@link ProxyGrantingTicket}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface ProxyGrantingTicketFactory<T extends ProxyGrantingTicket> extends TicketFactory {

    /**
     * Create the ticket object.
     *
     * @param ticket         the ticket
     * @param authentication the authentication
     * @return the ticket instance
     * @throws Throwable the throwable
     */
    T create(ServiceTicket ticket, Authentication authentication) throws Throwable;

}
