package org.apereo.cas.logout;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.slo.SingleLogoutRequest;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.ticket.TicketGrantingTicket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Transient;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Define a logout request for a service accessed by a user.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
@ToString
@Getter
@Setter
@Builder
public class DefaultSingleLogoutRequest implements SingleLogoutRequest {

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
    @org.springframework.data.annotation.Transient
    private final transient RegisteredService registeredService;

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    private final transient TicketGrantingTicket ticketGrantingTicket;

    /**
     * The status of the logout request.
     */
    @Builder.Default
    private LogoutRequestStatus status = LogoutRequestStatus.NOT_ATTEMPTED;

    /**
     * The http-logoutType or binding that should be used to send the message to the url.
     */
    @Builder.Default
    private final RegisteredServiceLogoutType logoutType = RegisteredServiceLogoutType.BACK_CHANNEL;

    /**
     * Additional settings relevant for the logout url.
     */
    @Builder.Default
    private Map<String, String> properties = new LinkedHashMap<>(0);
}
