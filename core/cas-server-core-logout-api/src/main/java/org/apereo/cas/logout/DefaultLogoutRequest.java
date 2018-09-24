package org.apereo.cas.logout;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.TicketGrantingTicket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Transient;
import java.net.URL;

/**
 * Define a logout request for a service accessed by a user.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
@ToString
@Getter
@Setter
@RequiredArgsConstructor
public class DefaultLogoutRequest implements LogoutRequest {

    /**
     * Generated serialVersionUID.
     */
    private static final long serialVersionUID = -6411421298859045022L;

    /**
     * The service ticket id.
     */
    private final String ticketId;

    /**
     * The service.
     */
    private final WebApplicationService service;

    private final URL logoutUrl;

    @JsonIgnore
    @Transient
    private final transient RegisteredService registeredService;

    @JsonIgnore
    @Transient
    private final transient TicketGrantingTicket ticketGrantingTicket;

    /**
     * The status of the logout request.
     */
    private LogoutRequestStatus status = LogoutRequestStatus.NOT_ATTEMPTED;

}
