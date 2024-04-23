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
public interface ProxyGrantingTicketFactory extends TicketFactory {

    /**
     * Create the ticket object.
     *
     * @param <T>            the type parameter
     * @param ticket         the ticket
     * @param authentication the authentication
     * @param clazz          the clazz
     * @return the ticket instance
     * @throws Throwable the throwable
     */
    <T extends ProxyGrantingTicket> T create(ServiceTicket ticket, Authentication authentication, Class<T> clazz) throws Throwable;

}
