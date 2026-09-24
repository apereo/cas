package org.apereo.cas.jmx.ticket;

import module java.base;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.Assert;

/**
 * This is {@link TicketRegistryManagedResource}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ManagedResource(description = "Ticket registry inventory, statistics and expired-ticket cleanup")
@RequiredArgsConstructor
public class TicketRegistryManagedResource {
    private final TicketRegistry ticketRegistry;

    private final ObjectProvider<TicketRegistryCleaner> ticketRegistryCleaner;

    /**
     * Gets tickets.
     *
     * @return the tickets
     */
    @ManagedOperation(description = "List all ticket identifiers; use getTicketsByPrefix for bounded results")
    public Collection<String> getTickets() {
        try (val stream = ticketRegistry.stream()) {
            return stream.map(Ticket::getId).collect(Collectors.toSet());
        }
    }

    /**
     * Gets ticket count.
     *
     * @return the ticket count
     */
    @ManagedAttribute(description = "Total tickets currently stored in the registry")
    public long getTicketCount() {
        try (val stream = ticketRegistry.stream()) {
            return stream.count();
        }
    }

    @ManagedAttribute(description = "Stored single sign-on sessions; a negative value means the count is unavailable")
    public long getSessionCount() {
        return ticketRegistry.sessionCount();
    }

    @ManagedAttribute(description = "Stored service tickets; a negative value means the count is unavailable")
    public long getServiceTicketCount() {
        return ticketRegistry.serviceTicketCount();
    }

    /**
     * List a bounded number of ticket identifiers matching a ticket type.
     *
     * @param prefix the exact ticket prefix
     * @param limit the maximum number of results
     * @return the ticket identifiers
     */
    @ManagedOperation(description = "List at most limit ticket identifiers for an exact prefix, such as TGT or ST")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "prefix", description = "Exact ticket prefix, without the trailing hyphen"),
        @ManagedOperationParameter(name = "limit", description = "Maximum number of results, from 1 to 1000")
    })
    public String[] getTicketsByPrefix(final String prefix, final int limit) {
        Assert.hasText(prefix, "Ticket prefix cannot be blank");
        Assert.isTrue(limit > 0 && limit <= 1_000, "Limit must be between 1 and 1000");
        try (val stream = ticketRegistry.stream()) {
            return stream.filter(ticket -> prefix.equals(ticket.getPrefix()))
                .limit(limit)
                .map(Ticket::getId)
                .toArray(String[]::new);
        }
    }

    /**
     * List a bounded number of sessions for a principal.
     *
     * @param principalId the principal identifier
     * @param limit the maximum number of results
     * @return the session identifiers
     */
    @ManagedOperation(description = "List at most limit active session identifiers for a principal")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "principalId", description = "Principal identifier"),
        @ManagedOperationParameter(name = "limit", description = "Maximum number of results, from 1 to 1000")
    })
    public String[] getSessionsFor(final String principalId, final int limit) {
        Assert.hasText(principalId, "Principal identifier cannot be blank");
        Assert.isTrue(limit > 0 && limit <= 1_000, "Limit must be between 1 and 1000");
        try (val stream = ticketRegistry.getSessionsFor(principalId)) {
            return stream.limit(limit).map(Ticket::getId).toArray(String[]::new);
        }
    }

    /**
     * Clean expired tickets through the configured registry cleaner.
     *
     * @return the number of tickets removed
     */
    @ManagedOperation(description = "Run the configured expired-ticket cleaner and return the number removed")
    public int clean() {
        val cleaner = Optional.ofNullable(ticketRegistryCleaner.getIfAvailable())
            .orElseThrow(() -> new IllegalStateException("No ticket registry cleaner is configured"));
        return cleaner.clean();
    }
}
