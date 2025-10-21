package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.util.Optional;

/**
 * This is {@link DefaultTicketRegistrySupport}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@RequiredArgsConstructor
@Getter
public class DefaultTicketRegistrySupport implements TicketRegistrySupport {

    private final TicketRegistry ticketRegistry;

    @Override
    public Authentication getAuthenticationFrom(final String ticketGrantingTicketId) {
        if (StringUtils.isBlank(ticketGrantingTicketId)) {
            return null;
        }
        val tgt = getTicketGrantingTicket(ticketGrantingTicketId);
        return Optional.ofNullable(tgt).map(TicketGrantingTicket::getAuthentication).orElse(null);
    }

    @Override
    public TicketGrantingTicket getTicketGrantingTicket(final String ticketGrantingTicketId) {
        if (StringUtils.isBlank(ticketGrantingTicketId)) {
            return null;
        }
        val tgt = (TicketGrantingTicket) getTicket(ticketGrantingTicketId);
        return tgt == null || tgt.isExpired() ? null : tgt;
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        if (StringUtils.isBlank(ticketId)) {
            return null;
        }
        return FunctionUtils.doAndHandle(() -> {
            val state = ticketRegistry.getTicket(ticketId, Ticket.class);
            return state == null || state.isExpired() ? null : state;
        });
    }

    @Override
    public Principal getAuthenticatedPrincipalFrom(final String ticketGrantingTicketId) {
        val auth = getAuthenticationFrom(ticketGrantingTicketId);
        return Optional.ofNullable(auth).map(Authentication::getPrincipal).orElse(null);
    }

    @Override
    public void updateAuthentication(final String ticketGrantingTicketId, final Authentication authentication) throws Exception {
        if (StringUtils.isNotBlank(ticketGrantingTicketId)) {
            val tgt = (TicketGrantingTicket) getTicket(ticketGrantingTicketId);
            if (tgt != null && !tgt.isExpired()) {
                tgt.getAuthentication().updateAttributes(authentication);
                ticketRegistry.updateTicket(tgt);
            }
        }
    }
}
