package org.apereo.cas.logout;

import org.apereo.cas.authentication.principal.WebApplicationService;

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

}
