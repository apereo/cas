package org.jasig.cas.ticket;

import org.jasig.cas.authentication.Authentication;

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
     * @return the t
     */
    <T extends ProxyGrantingTicket> T create(ServiceTicket ticket, Authentication authentication);
}
