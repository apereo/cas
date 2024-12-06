package org.apereo.cas.web.flow.actions;

import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;

/**
 * This is {@link FetchTicketGrantingTicketAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class FetchTicketGrantingTicketAction extends BaseCasWebflowAction {
    private final TicketRegistry ticketRegistry;

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val cookieResult = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        Optional.ofNullable(cookieResult).ifPresent(cookie -> populateWebflowWithTicketFromCookie(requestContext, cookie));
        return null;
    }

    private void populateWebflowWithTicketFromCookie(final RequestContext requestContext, final String ticketId) {
        LOGGER.debug("Attempting to locate ticket-granting ticket from cookie value [{}]", ticketId);
        val ticket = FunctionUtils.doAndHandle(() -> ticketRegistry.getTicket(ticketId, TicketGrantingTicket.class), throwable -> null).get();
        if (ticket != null) {
            LOGGER.debug("Found ticket-granting ticket [{}]", ticket.getId());
            WebUtils.putTicketGrantingTicket(requestContext, ticket);
            WebUtils.putTicketGrantingTicketInScopes(requestContext, ticket);
            WebUtils.putAuthentication(ticket.getAuthentication(), requestContext);
        }
    }
}
