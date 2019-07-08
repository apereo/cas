package org.apereo.cas.jmx.ticket;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.RequiredArgsConstructor;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.Collection;
import java.util.stream.Collectors;

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

    @ManagedOperation
    public Collection<String> getTickets() {
        return ticketRegistry
            .getTicketsStream()
            .map(Ticket::getId)
            .collect(Collectors.toSet());
    }
}
