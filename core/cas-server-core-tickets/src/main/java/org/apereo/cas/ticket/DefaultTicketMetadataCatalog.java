package org.apereo.cas.ticket;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This is {@link DefaultTicketMetadataCatalog}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultTicketMetadataCatalog implements TicketMetadataCatalog {
    private final Map<String, TicketDefinition> ticketMetadataMap = new HashMap<>();

    public DefaultTicketMetadataCatalog() {
    }

    @Override
    public TicketDefinition findTicketMetadata(final String ticketId) {
        return ticketMetadataMap.values().stream().filter(md -> ticketId.startsWith(md.getPrefix())).findFirst().get();
    }

    @Override
    public TicketDefinition findTicketMetadata(final Ticket ticket) {
        return findTicketMetadata(ticket.getPrefix());
    }

    @Override
    public void registerTicketMetadata(final TicketDefinition ticketDefinition) {
        ticketMetadataMap.put(ticketDefinition.getPrefix(), ticketDefinition);
    }

    @Override
    public void updateTicketMetadata(final TicketDefinition metadata) {
        registerTicketMetadata(metadata);
    }

    @Override
    public boolean containsTicketMetadata(final String ticketId) {
        return ticketMetadataMap.containsKey(ticketId);
    }

    /**
     * Find ticket implementation class class.
     *
     * @param ticketId the ticket id
     * @return the class
     */
    @Override
    public Class<? extends Ticket> findTicketImplementationClass(final String ticketId) {
        return findTicketMetadata(ticketId).getImplementationClass();
    }

    @Override
    public Collection<TicketDefinition> findAllTicketMetadata() {
        return new HashSet<>(ticketMetadataMap.values());
    }
}
