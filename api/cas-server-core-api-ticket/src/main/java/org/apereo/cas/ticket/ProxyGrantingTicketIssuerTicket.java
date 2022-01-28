package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link ProxyGrantingTicketIssuerTicket}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface ProxyGrantingTicketIssuerTicket extends Ticket {

    /**
     * Method to grant a {@link TicketGrantingTicket} from this service to the
     * authentication. Analogous to the {@link ProxyGrantingTicket}.
     *
     * @param id               The unique identifier for this ticket.
     * @param authentication   The Authentication we wish to grant a ticket for.
     * @param expirationPolicy expiration policy associated with this ticket
     * @return The ticket granting ticket.
     * @throws AbstractTicketException ticket exception thrown when generating the ticket
     * @since 4.2
     */
    ProxyGrantingTicket grantProxyGrantingTicket(String id,
                                                 Authentication authentication,
                                                 ExpirationPolicy expirationPolicy)
        throws AbstractTicketException;
}
