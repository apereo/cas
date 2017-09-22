package org.apereo.cas.ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;

/**
 * Interface for a Service Ticket. A service ticket is used to grant access to a
 * specific service for a principal. A Service Ticket is generally a one-time
 * use ticket.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY)
public interface ServiceTicket extends Ticket {

    /**
     * Prefix generally applied to unique ids generated
     * by UniqueTicketIdGenerator.
     */
    String PREFIX = "ST";

    /**
     * Retrieve the service this ticket was given for.
     *
     * @return the server.
     */
    Service getService();

    /**
     * Determine if this ticket was created at the same time as a
     * TicketGrantingTicket.
     *
     * @return true if it is, false otherwise.
     */
    boolean isFromNewLogin();

    /**
     * Attempts to ensure that the service specified matches the service associated with the ticket.
     *
     * @param service The incoming service to match this service ticket against.
     * @return true, if the match is successful.
     */
    boolean isValidFor(Service service);

    /**
     * Method to grant a TicketGrantingTicket from this service to the
     * authentication. Analogous to the ProxyGrantingTicket.
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
