package org.apereo.cas.logout;

import java.net.URL;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.WebApplicationService;
import lombok.ToString;
import lombok.Getter;

/**
 * Define a logout request for a service accessed by a user.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
@Slf4j
@ToString
@Getter
@Setter
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
}
