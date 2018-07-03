package org.apereo.cas.authentication.policy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;

import java.security.GeneralSecurityException;

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
    public boolean isSatisfiedBy(final Authentication authentication) throws Exception {
        try {
            final var authPrincipal = authentication.getPrincipal();
            final var count = this.ticketRegistry.getTickets(t -> {
                var pass = TicketGrantingTicket.class.isInstance(t) && !t.isExpired();
                if (pass) {
                    final var principal = TicketGrantingTicket.class.cast(t).getAuthentication().getPrincipal();
                    pass = principal.getId().equalsIgnoreCase(authPrincipal.getId());
                }
                return pass;
            }).count();
            if (count == 0) {
                LOGGER.debug("Authentication policy is satisfied with [{}]", authPrincipal.getId());
                return true;
            }
            LOGGER.warn("Authentication policy cannot be satisfied for principal [{}] because [{}] sessions currently exist",
                authPrincipal.getId(), count);
            return false;
        } catch (final Exception e) {
            throw new GeneralSecurityException(e);
        }
    }
}
