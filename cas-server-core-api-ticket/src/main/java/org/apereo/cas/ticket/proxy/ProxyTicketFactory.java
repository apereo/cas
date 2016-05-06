package org.apereo.cas.ticket.proxy;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;

/**
 * The {@link ProxyTicketFactory} is responsible for
 * creating instances of {@link ProxyTicket}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface ProxyTicketFactory extends TicketFactory {

    /**
     * Create the ticket object.
     *
     * @param <T>                  the type parameter
     * @param ticketGrantingTicket the ticket granting ticket
     * @param service              the service
     * @return the t
     */
    <T extends Ticket> T create(ProxyGrantingTicket ticketGrantingTicket,
                                Service service);
}
