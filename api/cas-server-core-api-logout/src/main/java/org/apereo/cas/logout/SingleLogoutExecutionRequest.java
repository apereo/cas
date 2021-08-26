package org.apereo.cas.logout;

import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

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
    private final Optional<HttpServletRequest> httpServletRequest = Optional.empty();

    @Builder.Default
    private final Optional<HttpServletResponse> httpServletResponse = Optional.empty();
}
