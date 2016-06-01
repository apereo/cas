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
     * @param <T>                   the type parameter
     * @param ticketGrantingTicket  the ticket granting ticket
     * @param service               the service
     * @param currentAuthentication current authentication event, may be null.
     * @return the t
     */
    <T extends Ticket> T create(TicketGrantingTicket ticketGrantingTicket,
                                Service service,
                                Authentication currentAuthentication);
}
