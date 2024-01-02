package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;

/**
 * The {@link ServiceTicketFactory} is responsible for
 * creating instances of {@link ServiceTicket}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface ServiceTicketFactory extends TicketFactory {

    /**
     * Create the ticket object.
     *
     * @param <T>                  the type parameter
     * @param ticketGrantingTicket the ticket granting ticket
     * @param service              the service
     * @param credentialProvided   current credential if provided as part of primary authn, may be false.
     * @param clazz                the clazz
     * @return the t
     * @throws Throwable the throwable
     */
    <T extends Ticket> T create(TicketGrantingTicket ticketGrantingTicket, Service service,
                                boolean credentialProvided, Class<T> clazz) throws Throwable;

    /**
     * Create service ticket.
     *
     * @param <T>                 the type parameter
     * @param service             the service
     * @param authentication      the authentication
     * @param credentialsProvided the credentials provided
     * @param serviceTicketClass  the service ticket class
     * @return the t
     * @throws Throwable the throwable
     */
    <T extends Ticket> T create(Service service, Authentication authentication,
                                boolean credentialsProvided, Class<T> serviceTicketClass) throws Throwable;
}
