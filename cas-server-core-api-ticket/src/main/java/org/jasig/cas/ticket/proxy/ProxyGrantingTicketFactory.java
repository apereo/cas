package org.jasig.cas.ticket.proxy;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.ticket.AbstractTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketFactory;

/**
 * The {@link ProxyGrantingTicketFactory} is responsible for
 * creating instances of {@link ProxyGrantingTicket}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface ProxyGrantingTicketFactory extends TicketFactory {

    /**
     * Create the ticket object.
     *
     * @param <T>            the type parameter
     * @param ticket         the ticket
     * @param authentication the authentication
     * @return the ticket instance
     * @throws AbstractTicketException the abstract ticket exception
     */
    <T extends ProxyGrantingTicket> T create(ServiceTicket ticket, Authentication authentication)
            throws AbstractTicketException;
}
