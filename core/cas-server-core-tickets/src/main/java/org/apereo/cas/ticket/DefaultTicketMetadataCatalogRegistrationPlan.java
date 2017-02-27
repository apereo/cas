package org.apereo.cas.ticket;

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
    public void registerTicketMetadata(final TicketMetadata ticketMetadata) {
        ticketMetadatas.add(ticketMetadata);
    }

    /**
     * Find ticket implementation class class.
     *
     * @param ticketId the ticket id
     * @return the class
     */
    public Class<? extends Ticket> findTicketImplementationClass(final String ticketId) {
        return ticketMetadatas.stream().filter(md -> ticketId.startsWith(md.getPrefix())).findFirst().get().getImplementationClass();
    }

    /**
     * Find ticket implementation class simple name string.
     *
     * @param ticketId the ticket id
     * @return the string
     */
    public String findTicketImplementationClassSimpleName(final String ticketId) {
        return findTicketImplementationClass(ticketId).getSimpleName();
    }
}
