package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * This is {@link DefaultTicketRegistrySupport}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@Transactional(transactionManager = "ticketTransactionManager")
@RequiredArgsConstructor
public class DefaultTicketRegistrySupport implements TicketRegistrySupport {

    private final TicketRegistry ticketRegistry;

    @Override
    public Authentication getAuthenticationFrom(final String ticketGrantingTicketId) throws RuntimeException {
        val tgt = this.ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        return tgt == null || tgt.isExpired() ? null : tgt.getAuthentication();
    }

    @Override
    public Principal getAuthenticatedPrincipalFrom(final String ticketGrantingTicketId) throws RuntimeException {
        val auth = getAuthenticationFrom(ticketGrantingTicketId);
        return auth == null ? null : auth.getPrincipal();
    }

    @Override
    public Map<String, Object> getPrincipalAttributesFrom(final String ticketGrantingTicketId) throws RuntimeException {
        val principal = getAuthenticatedPrincipalFrom(ticketGrantingTicketId);
        return principal == null ? null : principal.getAttributes();
    }

    @Override
    public void updateAuthentication(final String ticketGrantingTicketId, final Authentication authentication) {
        val tgt = this.ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        tgt.getAuthentication().update(authentication);
        this.ticketRegistry.updateTicket(tgt);
    }
}
