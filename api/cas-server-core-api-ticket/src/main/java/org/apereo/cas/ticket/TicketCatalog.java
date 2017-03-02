package org.apereo.cas.ticket;

import java.util.Collection;

/**
 * This is {@link TicketCatalog}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface TicketCatalog {

    /**
     * Register ticket definition.
     *
     * @param definition the definition
     */
    void register(TicketDefinition definition);

    /**
     * Update ticket definition.
     *
     * @param definition the definition
     */
    void update(TicketDefinition definition);

    /**
     * Contains ticket definition.
     *
     * @param ticketId the ticket id
     * @return the boolean
     */
    boolean contains(String ticketId);

    /**
     * Find ticket definition.
     *
     * @param ticketId the ticket id
     * @return the ticket definition
     */
    TicketDefinition find(String ticketId);

    /**
     * Find collection.
     *
     * @param ticketClass the ticket class
     * @return the collection
     */
    Collection<TicketDefinition> find(Class<Ticket> ticketClass);

    /**
     * Find ticket definition.
     *
     * @param ticket the ticket
     * @return the ticket definition
     */
    TicketDefinition find(Ticket ticket);

    /**
     * Find all ticket definition collection.
     *
     * @return the collection
     */
    Collection<TicketDefinition> findAll();
}
