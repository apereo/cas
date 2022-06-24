package org.apereo.cas.ticket;

/**
 * This is {@link ServiceTicketSessionTrackingPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@FunctionalInterface
public interface ServiceTicketSessionTrackingPolicy {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "serviceTicketSessionTrackingPolicy";

    /**
     * Track application attempt and access.
     * Typically, ticket-granting tickets keep track of applications
     * and service tickets for which they are authorized to issue tickets.
     *
     * @param ownerTicket   the owner ticket
     * @param serviceTicket the service ticket
     */
    void track(AuthenticatedServicesAwareTicketGrantingTicket ownerTicket,
               ServiceTicket serviceTicket);
}
