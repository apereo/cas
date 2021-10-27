package org.jasig.cas.ticket.proxy;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketGrantingTicket;

/**
 * Interface for a proxy granting ticket. A proxy-granting ticket is an opaque string that is
 * used by a service to obtain proxy tickets for obtaining access to a back-end service on behalf of a client.
 * Proxy-granting tickets are obtained from CAS upon validation of a service ticket or a proxy ticket.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface ProxyGrantingTicket extends TicketGrantingTicket {

    /** The prefix to use when generating an id for a Proxy Granting Ticket. */
    String PROXY_GRANTING_TICKET_PREFIX = "PGT";

    /** The prefix to use when generating an id for a Proxy Granting Ticket IOU. */
    String PROXY_GRANTING_TICKET_IOU_PREFIX = "PGTIOU";

    /**
     * Grant a proxy ticket for a specific service.
     *
     * @param id The unique identifier for this ticket.
     * @param service The service for which we are granting a ticket
     * @param expirationPolicy the expiration policy.
     * @param onlyTrackMostRecentSession track the most recent session by keeping the latest service ticket
     * @return the service ticket granted to a specific service for the
     * principal of the TicketGrantingTicket
     */
    ProxyTicket grantProxyTicket(String id, Service service,
                                 ExpirationPolicy expirationPolicy,
                                 boolean onlyTrackMostRecentSession);

}

