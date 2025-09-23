package org.apereo.cas.ticket.proxy;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TicketFactory;

/**
 * The {@link ProxyTicketFactory} is responsible for
 * creating instances of {@link ProxyTicket}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface ProxyTicketFactory<T extends ProxyTicket> extends TicketFactory {

    /**
     * Create the ticket object.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     * @param service              the service
     * @return the t
     * @throws Throwable the throwable
     */
    T create(ProxyGrantingTicket ticketGrantingTicket, Service service) throws Throwable;
}
