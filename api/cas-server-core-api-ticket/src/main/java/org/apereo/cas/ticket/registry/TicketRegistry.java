package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.jooq.lambda.Unchecked;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * Default bean name.
     */
    String BEAN_NAME = "ticketRegistry";

    /**
     * Add a ticket to the registry. Ticket storage is based on the ticket id.
     *
     * @param ticket The ticket we wish to add to the cache.
     * @return ticket
     * @throws Exception the exception
     */
    Ticket addTicket(Ticket ticket) throws Exception;

    /**
     * Save.
     *
     * @param toSave the to save
     */
    default List<? extends Ticket> addTicket(final Stream<? extends Ticket> toSave) {
        return toSave.parallel().map(Unchecked.function(this::addTicket)).filter(Objects::nonNull).collect(Collectors.toList());
    }

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
     * Gets ticket from registry using a predicate.
     *
     * @param ticketId  the ticket id
     * @param checkAndRemoveFromRegistry the predicate that tests the ticket and removes from registry when invalid
     * @param checkOnly the predicate that tests the ticket
     * @return the ticket
     */
    Ticket getTicket(String ticketId, Predicate<Ticket> checkAndRemoveFromRegistry, Predicate<Ticket> checkOnly);

    /**
     * Remove a specific ticket from the registry.
     * If ticket to delete is TGT then related service tickets are removed as well.
     *
     * @param ticketId The id of the ticket to delete.
     * @return the number of tickets deleted including children.
     * @throws Exception the exception
     */
    int deleteTicket(String ticketId) throws Exception;

    /**
     * Remove a specific ticket from the registry.
     * If ticket to delete is TGT then related service tickets, etc are removed as well.
     *
     * @param ticketId The id of the ticket to delete.
     * @return the number of tickets deleted including children.
     * @throws Exception the exception
     */
    int deleteTicket(Ticket ticketId) throws Exception;

    /**
     * Delete all tickets from the registry.
     *
     * @return the number of tickets deleted.
     */
    default long deleteAll() {
        return 0;
    }

    /**
     * Retrieve all tickets from the registry.
     *
     * @return collection of tickets currently stored in the registry. Tickets might or might not be valid i.e. expired.
     */
    default Collection<? extends Ticket> getTickets() {
        return List.of();
    }

    /**
     * Gets tickets as a stream having applied a predicate.
     * <p>
     * The returning stream may be bound to an IO channel (such as database connection),
     * so it should be properly closed after usage.
     *
     * @param predicate the predicate
     * @return the tickets
     */
    default Stream<? extends Ticket> getTickets(final Predicate<Ticket> predicate) {
        return stream().filter(predicate);
    }

    /**
     * Update the received ticket.
     *
     * @param ticket the ticket
     * @return the updated ticket
     * @throws Exception the exception
     */
    Ticket updateTicket(Ticket ticket) throws Exception;

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
     * <p>
     * The returning stream may be bound to an IO channel (such as database connection),
     * so it should be properly closed after usage.
     *
     * @return the tickets stream
     */
    default Stream<? extends Ticket> stream(final TicketRegistryStreamCriteria criteria) {
        return getTickets().parallelStream();
    }

    /**
     * Stream stream.
     *
     * @return the stream
     */
    default Stream<? extends Ticket> stream() {
        return stream(TicketRegistryStreamCriteria.builder().build());
    }

    /**
     * Count the number of single sign-on sessions
     * that are recorded in the ticket registry for
     * the given user name.
     *
     * @param principalId the principal id
     * @return the count
     */
    long countSessionsFor(String principalId);

    /**
     * Gets sessions for principal.
     *
     * @param principalId the principal id
     * @return the sessions for
     */
    default Stream<? extends Ticket> getSessionsFor(final String principalId) {
        return getTickets(ticket -> ticket instanceof final TicketGrantingTicket ticketGrantingTicket
            && !ticket.isExpired()
            && ticketGrantingTicket.getAuthentication().getPrincipal().getId().equals(principalId));
    }

    /**
     * Gets tickets with authentication attributes.
     *
     * @param queryAttributes the query attributes
     * @return the tickets with authentication attributes
     */
    Stream<? extends Ticket> getSessionsWithAttributes(Map<String, List<Object>> queryAttributes);

    /**
     * Allows the registry to hash the given identifier, which may be the ticket id or the principdl id, etc.
     *
     * @param id the id
     * @return the string
     */
    String digestIdentifier(String id);

    /**
     * Query the registry and return the results.
     * This operations allows one to interact with the registry
     * in raw form without a lot of post-processing of the ticket objects.
     * Registry implementations are to decide which criteria options they wish to support.
     *
     * @param criteria the criteria
     * @return the results
     */
    default List<? extends Serializable> query(final TicketRegistryQueryCriteria criteria) {
        return new ArrayList<>();
    }

    /**
     * Count the number of tickets, given a type or prefix
     * that might have been issued for given application.
     *
     * @param service the service
     * @return total count
     */
    default long countTicketsFor(final Service service) {
        return 0;
    }

    /**
     * Count all tickets.
     *
     * @return the total count.
     */
    default long countTickets() {
        return stream().count();
    }
}
