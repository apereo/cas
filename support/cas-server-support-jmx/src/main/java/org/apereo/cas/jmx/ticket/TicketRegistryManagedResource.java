package org.apereo.cas.jmx.ticket;

import module java.base;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * This is {@link TicketRegistryManagedResource}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ManagedResource
@RequiredArgsConstructor
public class TicketRegistryManagedResource {
    private final TicketRegistry ticketRegistry;

    /**
     * Gets tickets.
     *
     * @return the tickets
     */
    @ManagedOperation
    public Collection<String> getTickets() {
        try (val stream = ticketRegistry.stream()) {
            return stream.map(Ticket::getId).collect(Collectors.toSet());
        }
    }
}
