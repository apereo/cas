package org.apereo.cas.logout.slo;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.context.ApplicationContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link DefaultSingleLogoutRequestExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultSingleLogoutRequestExecutor implements SingleLogoutRequestExecutor {
    private final TicketRegistry ticketRegistry;

    private final LogoutManager logoutManager;

    private final ApplicationContext applicationContext;

    @Override
    public List<SingleLogoutRequestContext> execute(final String ticketId,
                                                    final HttpServletRequest request,
                                                    final HttpServletResponse response) {
        try {
            val ticket = ticketRegistry.getTicket(ticketId, Ticket.class);
            LOGGER.debug("Ticket [{}] found. Processing logout requests and then deleting the ticket...", ticket.getId());
            val clientInfo = ClientInfoHolder.getClientInfo();
            val logoutRequests = new ArrayList<SingleLogoutRequestContext>();
            if (ticket instanceof final TicketGrantingTicket tgt) {
                logoutRequests.addAll(logoutManager.performLogout(
                    SingleLogoutExecutionRequest.builder()
                        .ticketGrantingTicket(tgt)
                        .httpServletRequest(Optional.of(request))
                        .httpServletResponse(Optional.of(response))
                        .build()));
                applicationContext.publishEvent(new CasTicketGrantingTicketDestroyedEvent(this, tgt, clientInfo));
            }
            LOGGER.trace("Removing ticket [{}] from registry...", ticketId);
            ticketRegistry.deleteTicket(ticketId);
            return logoutRequests;
        } catch (final Exception e) {
            val msg = String.format("Ticket-granting ticket [%s] cannot be found in the ticket registry.", ticketId);
            LOGGER.debug(msg, e);
        }
        return new ArrayList<>(0);
    }
}
