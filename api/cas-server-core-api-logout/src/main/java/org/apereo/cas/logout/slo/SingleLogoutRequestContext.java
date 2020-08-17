package org.apereo.cas.logout.slo;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.logout.SingleLogoutExecutionRequest;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceLogoutType;

import java.io.Serializable;
import java.net.URL;
import java.util.Map;

/**
 * Identifies a CAS logout request and its properties.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public interface SingleLogoutRequestContext extends Serializable {
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
     * The execution context in which logout request is being handled.
     *
     * @return the ticket-granting ticket.
     */
    SingleLogoutExecutionRequest getExecutionRequest();

    /**
     * The http-logoutType or binding that should be used to send the message to the url.
     *
     * @return the logout type
     */
    RegisteredServiceLogoutType getLogoutType();

    /**
     * Gets properties.
     *
     * @return the properties
     */
    Map<String, String> getProperties();
}
