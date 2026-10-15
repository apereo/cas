package org.apereo.cas.mcp;

import module java.base;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.DigestUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link CasMcpSessionReferences}. A ticket-granting ticket id is a bearer token for the single sign-on session, so
 * MCP tools never return it. They return a session reference instead, a digest of the id that identifies the session
 * without granting access to it, and accept that reference back together with the username of the person.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@UtilityClass
public class CasMcpSessionReferences {
    /**
     * The prefix of a session reference.
     */
    public static final String PREFIX = "session:";

    private static final int DIGEST_LENGTH = 32;

    /**
     * Build the session reference of a ticket-granting ticket.
     *
     * @param ticketGrantingTicketId the ticket-granting ticket id
     * @return the session reference
     */
    public static String of(final String ticketGrantingTicketId) {
        return PREFIX + DigestUtils.sha256(ticketGrantingTicketId).substring(0, DIGEST_LENGTH);
    }

    /**
     * Whether the value is a session reference rather than a ticket id.
     *
     * @param value the value
     * @return true if the value is a session reference
     */
    public static boolean isReference(final @Nullable String value) {
        return StringUtils.startsWith(value, PREFIX);
    }

    /**
     * Find a ticket by its id, or a single sign-on session by its reference among the sessions of a person.
     *
     * @param ticketRegistry      the ticket registry
     * @param ticketIdOrReference the ticket id or session reference
     * @param username            the username of the person, needed for a session reference
     * @return the ticket, if found
     */
    public static Optional<Ticket> resolve(final TicketRegistry ticketRegistry, final String ticketIdOrReference,
                                           final @Nullable String username) {
        if (!isReference(ticketIdOrReference)) {
            return Optional.ofNullable(ticketRegistry.getTicket(ticketIdOrReference));
        }
        if (StringUtils.isBlank(username)) {
            return Optional.empty();
        }
        try (val sessions = ticketRegistry.getSessionsFor(username)) {
            return sessions
                .filter(ticket -> of(ticket.getId()).equals(ticketIdOrReference))
                .<Ticket>map(Ticket.class::cast)
                .findFirst();
        }
    }
}
