package org.jasig.cas.ticket.proxy;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketFactory;

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
