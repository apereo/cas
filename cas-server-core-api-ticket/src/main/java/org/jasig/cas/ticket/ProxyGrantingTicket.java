package org.jasig.cas.ticket;

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
}

