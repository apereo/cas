package org.jasig.cas.ticket.registry;

import java.util.Collection;

import org.jasig.cas.ticket.Ticket;

/**
 * Interface for a registry that stores tickets. The underlying registry can be
 * backed by anything from a normal HashMap to JGroups for having distributed
 * registries. It is up to specific implementations to determine their clean up
 * strategy. Strategies can include a manual clean up by a RegistryCleaner or a
 * more sophisticated strategy such as LRU.
 *
 * @author Scott Battaglia

 * @since 3.0.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public interface TicketRegistry {

    /**
     * Add a ticket to the registry. Ticket storage is based on the ticket id.
     *
     * @param ticket The ticket we wish to add to the cache.
     */
    void addTicket(Ticket ticket);

    /**
     * Retrieve a ticket from the registry. If the ticket retrieved does not
     * match the expected class, an InvalidTicketException is thrown.
     *
     * @param ticketId the id of the ticket we wish to retrieve.
     * @param clazz The expected class of the ticket we wish to retrieve.
     * @param <T> the generic ticket type to return that extends {@link Ticket}
     * @return the requested ticket.
     */
    <T extends Ticket> T getTicket(String ticketId, Class<? extends Ticket> clazz);

    /**
     * Retrieve a ticket from the registry.
     *
     * @param ticketId the id of the ticket we wish to retrieve
     * @return the requested ticket.
     */
    Ticket getTicket(String ticketId);

    /**
     * Remove a specific ticket from the registry.
     * If ticket to delete is TGT then related service tickets are removed as well.
     *
     * @param ticketId The id of the ticket to delete.
     * @return true if the ticket was removed and false if the ticket did not
     * exist.
     */
    boolean deleteTicket(String ticketId);

    /**
     * Retrieve all tickets from the registry.
     *
     * @return collection of tickets currently stored in the registry. Tickets
     * might or might not be valid i.e. expired.
     */
    Collection<Ticket> getTickets();
}
