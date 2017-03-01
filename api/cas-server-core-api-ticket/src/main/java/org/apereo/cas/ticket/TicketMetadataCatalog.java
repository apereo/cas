package org.apereo.cas.ticket;

import java.util.Collection;

/**
 * This is {@link TicketMetadataCatalog}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface TicketMetadataCatalog {

    /**
     * Register ticket metadata.
     *
     * @param metadata the metadata
     */
    void registerTicketMetadata(TicketDefinition metadata);

    /**
     * Update ticket metadata.
     *
     * @param metadata the metadata
     */
    void updateTicketMetadata(TicketDefinition metadata);

    /**
     * Contains ticket metadata.
     *
     * @param ticketId the ticket id
     * @return the boolean
     */
    boolean containsTicketMetadata(String ticketId);

    /**
     * Find ticket metadata ticket metadata.
     *
     * @param ticketId the ticket id
     * @return the ticket metadata
     */
    TicketDefinition findTicketMetadata(String ticketId);

    /**
     * Find ticket metadata ticket metadata.
     *
     * @param ticket the ticket
     * @return the ticket metadata
     */
    TicketDefinition findTicketMetadata(Ticket ticket);

    /**
     * Find all ticket metadata collection.
     *
     * @return the collection
     */
    Collection<TicketDefinition> findAllTicketMetadata();
}
