package org.apereo.cas.logout.slo;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.SingleLogoutExecutionRequest;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private final CentralAuthenticationService centralAuthenticationService;

    private final LogoutManager logoutManager;

    private final ApplicationContext applicationContext;

    @Override
    public List<SingleLogoutRequestContext> execute(final String ticketId,
                                                    final HttpServletRequest request,
                                                    final HttpServletResponse response) {
        val ca = AuthenticationCredentialsThreadLocalBinder.getCurrentAuthentication();
        try {
            val ticket = centralAuthenticationService.getTicket(ticketId, Ticket.class);
            LOGGER.debug("Ticket [{}] found. Processing logout requests and then deleting the ticket...", ticket.getId());

            val logoutRequests = new ArrayList<SingleLogoutRequestContext>();
            if (ticket instanceof TicketGrantingTicket) {
                val tgt = (TicketGrantingTicket) ticket;
                AuthenticationCredentialsThreadLocalBinder.bindCurrent(tgt.getAuthentication());
                logoutRequests.addAll(logoutManager.performLogout(
                    SingleLogoutExecutionRequest.builder()
                        .ticketGrantingTicket(tgt)
                        .httpServletRequest(Optional.of(request))
                        .httpServletResponse(Optional.of(response))
                        .build()));
                applicationContext.publishEvent(new CasTicketGrantingTicketDestroyedEvent(this, tgt));
            }
            LOGGER.trace("Removing ticket [{}] from registry...", ticketId);
            centralAuthenticationService.deleteTicket(ticketId);
            return logoutRequests;
        } catch (final InvalidTicketException e) {
            val msg = String.format("Ticket-granting ticket [%s] cannot be found in the ticket registry.", ticketId);
            LOGGER.debug(msg, e);
        } finally {
            AuthenticationCredentialsThreadLocalBinder.bindCurrent(ca);
        }
        return new ArrayList<>(0);
    }
}
