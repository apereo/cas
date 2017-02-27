package org.apereo.cas.ticket;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link DefaultTicketMetadataCatalogRegistrationPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultTicketMetadataCatalogRegistrationPlan implements TicketMetadataCatalogRegistrationPlan {
    private final Set<TicketMetadata> ticketMetadatas = new HashSet<>();

    public DefaultTicketMetadataCatalogRegistrationPlan() {
    }

    @Override
    public TicketMetadata findTicketMetadata(final String ticketId) {
        return ticketMetadatas.stream().filter(md -> ticketId.startsWith(md.getPrefix())).findFirst().get();
    }

    @Override
    public TicketMetadata findTicketMetadata(final Ticket ticket) {
        return findTicketMetadata(ticket.getPrefix());
    }

    @Override
    public void registerTicketMetadata(final TicketMetadata ticketMetadata) {
        ticketMetadatas.add(ticketMetadata);
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
        return new HashSet<>(ticketMetadatas);
    }
}
