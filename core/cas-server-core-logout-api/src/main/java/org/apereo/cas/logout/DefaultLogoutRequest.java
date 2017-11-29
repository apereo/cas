package org.apereo.cas.logout;

import java.net.URL;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.principal.WebApplicationService;

/**
 * Define a logout request for a service accessed by a user.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public class DefaultLogoutRequest implements LogoutRequest {

    /** Generated serialVersionUID. */
    private static final long serialVersionUID = -6411421298859045022L;

    /** The service ticket id. */
    private final String ticketId;

    /** The service. */
    private final WebApplicationService service;

    /** The status of the logout request. */
    private LogoutRequestStatus status = LogoutRequestStatus.NOT_ATTEMPTED;

    private final URL logoutUrl;

    /**
     * Build a logout request from ticket identifier and service.
     * Default status is {@link LogoutRequestStatus#NOT_ATTEMPTED}.
     *
     * @param ticketId the service ticket id.
     * @param service the service.
     * @param logoutUrl the logout url
     */
    public DefaultLogoutRequest(final String ticketId, final WebApplicationService service, final URL logoutUrl) {
        this.ticketId = ticketId;
        this.service = service;
        this.logoutUrl = logoutUrl;
    }

    @Override
    public LogoutRequestStatus getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(final LogoutRequestStatus status) {
        this.status = status;
    }

    @Override
    public String getTicketId() {
        return this.ticketId;
    }

    @Override
    public WebApplicationService getService() {
        return this.service;
    }

    @Override
    public URL getLogoutUrl() {
        return this.logoutUrl;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ticketId", this.ticketId)
                .append("service", this.service)
                .append("status", this.status)
                .toString();
    }
}
