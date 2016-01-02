package org.jasig.cas.ticket;

import org.jasig.cas.authentication.principal.Service;

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
     * @param credentialsProvided  the credentials provided
     * @return the t
     */
    <T extends Ticket> T create(TicketGrantingTicket ticketGrantingTicket,
                                Service service,
                                boolean credentialsProvided);
}
