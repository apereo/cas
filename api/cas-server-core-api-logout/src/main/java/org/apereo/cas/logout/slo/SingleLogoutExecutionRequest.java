package org.apereo.cas.logout.slo;

import module java.base;
import org.apereo.cas.ticket.TicketGrantingTicket;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link SingleLogoutExecutionRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SuperBuilder
@Getter
public class SingleLogoutExecutionRequest {
    private final TicketGrantingTicket ticketGrantingTicket;

    @Builder.Default
    @JsonIgnore
    private final Optional<HttpServletRequest> httpServletRequest = Optional.empty();

    @Builder.Default
    @JsonIgnore
    private final Optional<HttpServletResponse> httpServletResponse = Optional.empty();
}
