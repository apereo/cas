package org.apereo.cas.ticket;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This is {@link DefaultTicketMetadataRegistrationPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultTicketMetadataRegistrationPlan implements TicketMetadataRegistrationPlan {
    private final Map<String, TicketMetadata> ticketMetadataMap = new HashMap<>();

    public DefaultTicketMetadataRegistrationPlan() {
    }

    @Override
    public TicketMetadata findTicketMetadata(final String ticketId) {
        return ticketMetadataMap.values().stream().filter(md -> ticketId.startsWith(md.getPrefix())).findFirst().get();
    }

    @Override
    public TicketMetadata findTicketMetadata(final Ticket ticket) {
        return findTicketMetadata(ticket.getPrefix());
    }

    @Override
    public void registerTicketMetadata(final TicketMetadata ticketMetadata) {
        ticketMetadataMap.put(ticketMetadata.getPrefix(), ticketMetadata);
    }

    @Override
    public void updateTicketMetadata(final TicketMetadata metadata) {
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
    public Collection<TicketMetadata> findAllTicketMetadata() {
        return new HashSet<>(ticketMetadataMap.values());
    }
}
