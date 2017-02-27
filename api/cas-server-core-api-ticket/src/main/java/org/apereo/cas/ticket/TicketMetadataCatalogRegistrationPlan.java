package org.apereo.cas.ticket;

import java.util.Collection;

/**
 * This is {@link TicketMetadataCatalogRegistrationPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface TicketMetadataCatalogRegistrationPlan {

    /**
     * Register ticket metadata.
     *
     * @param metadata the metadata
     */
    void registerTicketMetadata(TicketMetadata metadata);

    /**
     * Find ticket metadata ticket metadata.
     *
     * @param ticketId the ticket id
     * @return the ticket metadata
     */
    TicketMetadata findTicketMetadata(String ticketId);

    /**
     * Find all ticket metadata collection.
     *
     * @return the collection
     */
    Collection<TicketMetadata> findAllTicketMetadata();

    /**
     * Find ticket implementation class.
     *
     * @param ticketId the ticket id
     * @return the class
     */
    Class<? extends Ticket> findTicketImplementationClass(String ticketId);
    
}
