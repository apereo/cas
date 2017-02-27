package org.apereo.cas.ticket;

import java.util.Collection;

/**
 * This is {@link TicketMetadataRegistrationPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface TicketMetadataRegistrationPlan {

    /**
     * Register ticket metadata.
     *
     * @param metadata the metadata
     */
    void registerTicketMetadata(TicketMetadata metadata);

    /**
     * Update ticket metadata.
     *
     * @param metadata the metadata
     */
    void updateTicketMetadata(TicketMetadata metadata);

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
    TicketMetadata findTicketMetadata(String ticketId);

    /**
     * Find ticket metadata ticket metadata.
     *
     * @param ticket the ticket
     * @return the ticket metadata
     */
    TicketMetadata findTicketMetadata(Ticket ticket);

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
