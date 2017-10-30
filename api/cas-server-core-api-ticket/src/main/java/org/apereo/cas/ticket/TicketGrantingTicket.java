package org.apereo.cas.ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Interface for a ticket granting ticket. A TicketGrantingTicket is the main
 * access into the CAS service layer. Without a TicketGrantingTicket, a user of
 * CAS cannot do anything.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface TicketGrantingTicket extends Ticket {

    /**
     * The prefix to use when generating an id for a Ticket Granting Ticket.
     */
    String PREFIX = "TGT";

    /**
     * Method to retrieve the authentication.
     *
     * @return the authentication
     */
    Authentication getAuthentication();

    /**
     * Grant a ServiceTicket for a specific service.
     *
     * @param id                         The unique identifier for this ticket.
     * @param service                    The service for which we are granting a ticket
     * @param expirationPolicy           the expiration policy.
     * @param credentialProvided         current credential event for issuing this ticket. Could be null.
     * @param onlyTrackMostRecentSession track the most recent session by keeping the latest service ticket
     * @return the service ticket granted to a specific service for the principal of the TicketGrantingTicket
     */
    ServiceTicket grantServiceTicket(String id, Service service,
                                     ExpirationPolicy expirationPolicy,
                                     boolean credentialProvided,
                                     boolean onlyTrackMostRecentSession);

    /**
     * Gets an immutable map of service ticket and services accessed by this ticket-granting ticket.
     *
     * @return an immutable map of service ticket and services accessed by this ticket-granting ticket.
     */
    Map<String, Service> getServices();

    /**
     * Gets proxy granting tickets created by this TGT.
     *
     * @return the proxy granting tickets
     */
    Map<String, Service> getProxyGrantingTickets();

    /**
     * Remove all services of the TGT (at logout).
     */
    void removeAllServices();

    /**
     * Mark a ticket as expired.
     */
    void markTicketExpired();

    /**
     * Convenience method to determine if the TicketGrantingTicket is the root
     * of the hierarchy of tickets.
     *
     * @return true if it has no parent, false otherwise.
     */
    boolean isRoot();

    /**
     * Gets the ticket-granting ticket at the root of the ticket hierarchy.
     *
     * @return Non -null root ticket-granting ticket.
     */
    TicketGrantingTicket getRoot();

    /**
     * Gets all authentications ({@link #getAuthentication()} from this
     * instance and all dependent tickets that reference this one.
     *
     * @return Non -null list of authentication associated with this ticket in leaf-first order.
     */
    List<Authentication> getChainedAuthentications();


    /**
     * Gets the service that produced a proxy-granting ticket.
     *
     * @return Service that produced proxy-granting ticket or null if this is not a proxy-granting ticket.
     * @since 4.1
     */
    Service getProxiedBy();

    /**
     * Gets descendant tickets. These are generally ticket ids
     * whose life-line is separate from the TGT until and unless
     * the TGT goes away entirely. Things such as OAuth access tokens
     * are a good example of such linked tickets.
     *
     * @return the descendant tickets
     * @since 5.1
     */
    default Collection<String> getDescendantTickets() {
        return new HashSet<>(0);
    }
}
