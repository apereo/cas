package org.apereo.cas.ticket;

import org.springframework.core.OrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

    @Override
    public Collection<TicketDefinition> findAllTicketMetadata() {
        final List list = new ArrayList<>(ticketMetadataMap.values());
        OrderComparator.sort(list);
        return list;
    }
}
