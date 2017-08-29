package org.apereo.cas.ticket.registry;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apereo.cas.ticket.Ticket;

/**
 * Interface for a registry that stores tickets. The underlying registry can be
 * backed by anything from a normal HashMap to JGroups for having distributed
 * registries. It is up to specific implementations to determine their clean up
 * strategy. Strategies can include a manual clean up by a registry cleaner or a
 * more sophisticated strategy such as LRU.
 *
 * @author Scott Battaglia
 * @since 3.0.0
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
     * @param <T>      the generic ticket type to return that extends {@link Ticket}
     * @param ticketId the id of the ticket we wish to retrieve.
     * @param clazz    The expected class of the ticket we wish to retrieve.
     * @return the requested ticket.
     */
    <T extends Ticket> T getTicket(String ticketId, Class<T> clazz);

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
     * @return the number of tickets deleted including children.
     */
    int deleteTicket(String ticketId);

    /**
     * Delete all tickets from the registry.
     *
     * @return the number of tickets deleted.
     */
    long deleteAll();

    /**
     * Retrieve all tickets from the registry.
     *
     * @return collection of tickets currently stored in the registry. Tickets might or might not be valid i.e. expired.
     */
    Collection<Ticket> getTickets();

    /**
     * Gets tickets as a stream having applied a predicate.
     *
     * @param predicate the predicate
     * @return the tickets
     */
    default Stream<Ticket> getTickets(final Predicate<Ticket> predicate) {
        return getTicketsStream().filter(predicate);
    }
    
    /**
     * Update the received ticket.
     *
     * @param ticket the ticket
     * @return the updated ticket
     */
    Ticket updateTicket(Ticket ticket);

    /**
     * Computes the number of SSO sessions stored in the ticket registry.
     *
     * @return Number of ticket-granting tickets in the registry at time of invocation or {@link Integer#MIN_VALUE} if unknown.
     */
    long sessionCount();

    /**
     * Computes the number of service tickets stored in the ticket registry.
     *
     * @return Number of service tickets in the registry at time of invocation or {@link Integer#MIN_VALUE} if unknown.
     */
    long serviceTicketCount();

    /**
     * Gets tickets stream.
     *
     * @return the tickets stream
     */
    default Stream<Ticket> getTicketsStream() {
        return getTickets().stream();
    }
    
}
