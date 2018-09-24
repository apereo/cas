package org.apereo.cas.logout;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.TicketGrantingTicket;

import java.io.Serializable;
import java.net.URL;

/**
 * Identifies a CAS logout request and its properties.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public interface LogoutRequest extends Serializable {
    /**
     * Gets status of the request.
     *
     * @return the status
     */
    LogoutRequestStatus getStatus();

    /**
     * Sets status of the request.
     *
     * @param status the status
     */
    void setStatus(LogoutRequestStatus status);

    /**
     * Gets ticket id.
     *
     * @return the ticket id
     */
    String getTicketId();

    /**
     * Gets service.
     *
     * @return the service
     */
    WebApplicationService getService();

    /**
     * Gets logout url.
     *
     * @return the logout url
     */
    URL getLogoutUrl();

    /**
     * Registered service policy linked to this request and service.
     *
     * @return registered service instance.
     */
    RegisteredService getRegisteredService();

    /**
     * Ticket instance that is being destroyed, initiating the logout request.
     * @return the ticket-granting ticket.
     */
    TicketGrantingTicket getTicketGrantingTicket();
}
