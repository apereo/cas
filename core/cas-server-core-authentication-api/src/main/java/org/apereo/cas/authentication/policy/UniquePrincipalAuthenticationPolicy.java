package org.apereo.cas.authentication.policy;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;

import java.security.GeneralSecurityException;
import java.util.stream.Stream;

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
@AllArgsConstructor
public class UniquePrincipalAuthenticationPolicy implements AuthenticationPolicy {
    private final TicketRegistry ticketRegistry;

    @Override
    public boolean isSatisfiedBy(final Authentication authentication) throws Exception {
        try {
            final Principal authPrincipal = authentication.getPrincipal();
            try (Stream<Ticket> ticketsStream =
                         this.ticketRegistry.getTickets(t -> isSamePrincipalId(t, authPrincipal))) {
                final long count = ticketsStream.count();
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

    private boolean isSamePrincipalId(final Ticket t, final Principal p) {
        boolean pass = TicketGrantingTicket.class.isInstance(t) && !t.isExpired();
        if (pass) {
            final Principal principal = TicketGrantingTicket.class.cast(t).getAuthentication().getPrincipal();
            pass = principal.getId().equalsIgnoreCase(p.getId());
        }
        return pass;
    }
}
