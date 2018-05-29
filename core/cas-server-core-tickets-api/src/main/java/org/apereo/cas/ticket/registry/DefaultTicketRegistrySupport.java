package org.apereo.cas.ticket.registry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.TicketGrantingTicket;
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
@Slf4j
@RequiredArgsConstructor
public class DefaultTicketRegistrySupport implements TicketRegistrySupport {

    private final TicketRegistry ticketRegistry;

    @Override
    public Authentication getAuthenticationFrom(final String ticketGrantingTicketId) throws RuntimeException {
        final var tgt = this.ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        return tgt == null || tgt.isExpired() ? null : tgt.getAuthentication();
    }

    @Override
    public Principal getAuthenticatedPrincipalFrom(final String ticketGrantingTicketId) throws RuntimeException {
        final var auth = getAuthenticationFrom(ticketGrantingTicketId);
        return auth == null ? null : auth.getPrincipal();
    }

    @Override
    public Map<String, Object> getPrincipalAttributesFrom(final String ticketGrantingTicketId) throws RuntimeException {
        final var principal = getAuthenticatedPrincipalFrom(ticketGrantingTicketId);
        return principal == null ? null : principal.getAttributes();
    }

    @Override
    public void updateAuthentication(final String ticketGrantingTicketId, final Authentication authentication) {
        final var tgt = this.ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        tgt.getAuthentication().update(authentication);
        this.ticketRegistry.updateTicket(tgt);
    }
}
