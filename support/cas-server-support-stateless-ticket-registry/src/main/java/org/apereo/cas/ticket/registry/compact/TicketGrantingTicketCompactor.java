package org.apereo.cas.ticket.registry.compact;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.SimplePrincipal;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link TicketGrantingTicketCompactor}.
 * The ticket is kept as the serialized form of a copy that carries no id, since the id is the compact ticket itself,
 * and none of the maps that track granted tickets, since the stateless registry does not track single sign-on sessions.
 * <p>
 * When the authenticated principal is a {@link SimplePrincipal}, its attributes and those of the principals
 * in the authentication handler results are not kept in the ticket. They are resolved again from
 * the attribute sources through the {@link PrincipalResolver} each time the ticket is expanded, keeping
 * the principal id the ticket carries. Credentials, authentication attributes and other principal types
 * are kept as they are. A failure to resolve the principal fails the expansion, and the ticket is treated as not found.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class TicketGrantingTicketCompactor implements TicketCompactor<TicketGrantingTicket> {
    private static final int TICKET_INDEX = 2;

    private final TicketSerializationManager ticketSerializationManager;

    private final ObjectProvider<PrincipalResolver> principalResolver;

    @Override
    public void compactFields(final List<String> fields, final Ticket ticket) {
        fields.add(ticketSerializationManager.serializeTicket(toSessionTicket(ticket)));
    }

    @Override
    public Class<TicketGrantingTicket> getTicketType() {
        return TicketGrantingTicket.class;
    }

    @Override
    public Ticket expand(final String compactTicket) throws Throwable {
        val structure = parse(compactTicket, TICKET_INDEX + 1);
        val ticket = ticketSerializationManager.deserializeTicket(structure.get(TICKET_INDEX), getTicketType());
        if (isPrincipalResolved(ticket)) {
            val sessionTicket = (TicketGrantingTicketImpl) ticket;
            return copyOf(sessionTicket, withResolvedPrincipal(Objects.requireNonNull(sessionTicket.getAuthentication())));
        }
        return ticket;
    }

    private Authentication withResolvedPrincipal(final Authentication authentication) throws Throwable {
        val resolver = principalResolver.getIfAvailable();
        if (resolver == null) {
            return authentication;
        }
        val principal = authentication.getPrincipal();
        val resolved = resolver.resolve(new BasicIdentifiableCredential(principal.getId()),
            Optional.of(principal), Optional.empty(), Optional.empty());
        if (resolved == null) {
            return authentication;
        }
        return DefaultAuthenticationBuilder
            .newInstance(authentication)
            .setPrincipal(principal.withAttributes(new HashMap<>(resolved.getAttributes())))
            .build();
    }

    private static Ticket toSessionTicket(final Ticket ticket) {
        if (ticket.getClass() != TicketGrantingTicketImpl.class) {
            return ticket;
        }
        val source = (TicketGrantingTicketImpl) ticket;
        val authentication = Objects.requireNonNull(source.getAuthentication());
        return copyOf(source, isPrincipalResolved(source) ? withoutPrincipalAttributes(authentication) : authentication);
    }

    private static boolean isPrincipalResolved(final Ticket ticket) {
        return ticket.getClass() == TicketGrantingTicketImpl.class
            && ((TicketGrantingTicketImpl) ticket).getAuthentication() instanceof final Authentication authentication
            && authentication.getPrincipal() instanceof SimplePrincipal;
    }

    private static Authentication withoutPrincipalAttributes(final Authentication authentication) {
        val successes = new LinkedHashMap<String, AuthenticationHandlerExecutionResult>();
        authentication.getSuccesses().forEach((name, result) -> successes.put(name,
            new DefaultAuthenticationHandlerExecutionResult(result.getHandlerName(), result.getCredential(),
                withoutAttributes(result.getPrincipal()), result.getWarnings())));
        return DefaultAuthenticationBuilder
            .newInstance(authentication)
            .setPrincipal(authentication.getPrincipal().withAttributes(new HashMap<>()))
            .setSuccesses(successes)
            .build();
    }

    private static @Nullable Principal withoutAttributes(final @Nullable Principal principal) {
        return principal instanceof SimplePrincipal ? principal.withAttributes(new HashMap<>()) : principal;
    }

    private static TicketGrantingTicketImpl copyOf(final TicketGrantingTicketImpl source, final Authentication authentication) {
        val sessionTicket = new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX,
            authentication, source.getExpirationPolicy());
        sessionTicket.setCreationTime(source.getCreationTime());
        sessionTicket.setLastTimeUsed(source.getLastTimeUsed());
        sessionTicket.setPreviousTimeUsed(source.getPreviousTimeUsed());
        sessionTicket.setCountOfUses(source.getCountOfUses());
        sessionTicket.setTenantId(source.getTenantId());
        sessionTicket.setProperties(new HashMap<>(source.getProperties()));
        return sessionTicket;
    }
}
