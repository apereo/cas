package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.security.GeneralSecurityException;
import java.util.Set;

/**
 * This is {@link UniquePrincipalAuthenticationPolicy}
 * that prevents authentication if the same principal id
 * is found more than one in the registry. This effectively forces
 * each user to have a single and unique SSO session, disallowing
 * multiple logins.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class UniquePrincipalAuthenticationPolicy implements AuthenticationPolicy {
    private final TicketRegistry ticketRegistry;

    @Override
    public boolean isSatisfiedBy(final Authentication authentication, final Set<AuthenticationHandler> authenticationHandlers) throws Exception {
        try {
            val authPrincipal = authentication.getPrincipal();
            try (val ticketsStream =
                         this.ticketRegistry.getTickets(t -> isSamePrincipalId(t, authPrincipal))) {
                val count = ticketsStream.count();
                if (count == 0) {
                    LOGGER.debug("Authentication policy is satisfied with [{}]", authPrincipal.getId());
                    return true;
                }
                LOGGER.warn("Authentication policy cannot be satisfied for principal [{}] because [{}] sessions currently exist",
                        authPrincipal.getId(), count);
                return false;
            }
        } catch (final Exception e) {
            throw new GeneralSecurityException(e);
        }
    }

    private static boolean isSamePrincipalId(final Ticket t, final Principal p) {
        return FunctionUtils.doIf(TicketGrantingTicket.class.isInstance(t) && !t.isExpired(),
            () -> {
                val principal = TicketGrantingTicket.class.cast(t).getAuthentication().getPrincipal();
                return principal.getId().equalsIgnoreCase(p.getId());
            },
            () -> Boolean.TRUE).get();
    }
}
