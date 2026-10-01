package org.apereo.cas.ticket.registry.compact;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.serialization.StringSerializer;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link TicketGrantingTicketCompactor}.
 * The ticket is kept as its authentication, without principal attributes, next to its creation and expiration times.
 * The id is the compact ticket itself, and the tickets it has granted are not kept, since the stateless registry
 * does not track single sign-on sessions.
 * <p>
 * Expanding the ticket resolves the principal attributes again from the attribute sources through the
 * {@link PrincipalResolver}, keeping the principal id the ticket carries, and creates the ticket
 * through the {@link TicketGrantingTicketFactory} with its original creation time and expiration instant.
 * A failure to resolve the principal fails the expansion, and the ticket is treated as not found.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class TicketGrantingTicketCompactor implements TicketCompactor<TicketGrantingTicket> {
    private static final int AUTHENTICATION_INDEX = 2;

    private final ObjectProvider<TicketFactory> ticketFactory;

    private final ObjectProvider<PrincipalResolver> principalResolver;

    private final StringSerializer<Authentication> authenticationSerializer;

    @Override
    public void compactFields(final List<String> fields, final Ticket ticket) {
        val authentication = Objects.requireNonNull(((AuthenticationAwareTicket) ticket).getAuthentication(),
            "Ticket must carry an authentication");
        fields.add(authenticationSerializer.toString(withoutPrincipalAttributes(authentication)));
    }

    @Override
    public Class<TicketGrantingTicket> getTicketType() {
        return TicketGrantingTicket.class;
    }

    @Override
    public Ticket expand(final String compactTicket) throws Throwable {
        val structure = parse(compactTicket, AUTHENTICATION_INDEX + 1);
        val authentication = withResolvedPrincipal(authenticationSerializer.from(structure.get(AUTHENTICATION_INDEX)));
        val factory = (TicketGrantingTicketFactory<?>) ticketFactory.getObject().get(getTicketType());
        val ticketGrantingTicket = factory.create(authentication, null);
        ticketGrantingTicket.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        ticketGrantingTicket.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        return ticketGrantingTicket;
    }

    private Authentication withResolvedPrincipal(final Authentication authentication) throws Throwable {
        val principal = authentication.getPrincipal();
        val resolved = principalResolver.getObject().resolve(new BasicIdentifiableCredential(principal.getId()),
            Optional.of(principal), Optional.empty(), Optional.empty());
        if (resolved == null) {
            return authentication;
        }
        return DefaultAuthenticationBuilder
            .newInstance(authentication)
            .setPrincipal(principal.withAttributes(new HashMap<>(resolved.getAttributes())))
            .build();
    }

    private static Authentication withoutPrincipalAttributes(final Authentication authentication) {
        val successes = new LinkedHashMap<String, AuthenticationHandlerExecutionResult>();
        authentication.getSuccesses().forEach((name, result) -> {
            val principal = result.getPrincipal();
            successes.put(name, new DefaultAuthenticationHandlerExecutionResult(result.getHandlerName(), result.getCredential(),
                principal == null ? null : principal.withoutAttributes(), result.getWarnings()));
        });
        return DefaultAuthenticationBuilder
            .newInstance(authentication)
            .setPrincipal(authentication.getPrincipal().withoutAttributes())
            .setSuccesses(successes)
            .build();
    }
}
