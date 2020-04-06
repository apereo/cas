package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;

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
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
public class UniquePrincipalAuthenticationPolicy extends BaseAuthenticationPolicy {
    private static final long serialVersionUID = 3974114391376732470L;

    private static boolean isSamePrincipalId(final Ticket t, final Principal p) {
        return FunctionUtils.doIf(TicketGrantingTicket.class.isInstance(t) && !t.isExpired(),
            () -> {
                val principal = TicketGrantingTicket.class.cast(t).getAuthentication().getPrincipal();
                return principal.getId().equalsIgnoreCase(p.getId());
            },
            () -> Boolean.TRUE).get();
    }

    @Override
    public boolean isSatisfiedBy(final Authentication authentication,
                                 final Set<AuthenticationHandler> authenticationHandlers,
                                 final ConfigurableApplicationContext applicationContext) throws Exception {
        try {
            val ticketRegistry = applicationContext.getBean("ticketRegistry", TicketRegistry.class);
            val authPrincipal = authentication.getPrincipal();
            try (val ticketsStream = ticketRegistry.getTickets(t -> isSamePrincipalId(t, authPrincipal))) {
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
}
