package org.apereo.cas.ticket;

import java.util.Collection;
import java.util.Optional;

/**
 * This is {@link TicketCatalog}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface TicketCatalog {

    /**
     * Implementation bean name.
     */
    String BEAN_NAME = "ticketCatalog";

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
     * @return true/false
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
     * Find ticket definition.
     *
     * @param ticket the ticket
     * @return the ticket definition
     */
    TicketDefinition find(Ticket ticket);

    /**
     * Find all ticket definitions that implement the given ticketClass.
     *
     * @param ticketClass the ticket class
     * @return the matching ticket definitions
     */
    Collection<TicketDefinition> findTicketImplementations(Class<? extends Ticket> ticketClass);

    /**
     * Find the ticket definition that matches this exact class.
     *
     * @param ticketClass the ticket class
     * @return the optional
     */
    Optional<TicketDefinition> findTicketDefinition(Class<? extends Ticket> ticketClass);

    /**
     * Find all ticket definition collection.
     *
     * @return the collection
     */
    Collection<TicketDefinition> findAll();
}
