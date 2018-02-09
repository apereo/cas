package org.apereo.cas.support.jpa;

import org.apereo.cas.ticket.Ticket;

import javax.persistence.TypedQuery;
import java.util.stream.Stream;

/**
 * This this {@link JpaStreamer}. Abstracts the process of turning a query into a stream of tickets to allow JPA provider independence.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@FunctionalInterface
public interface JpaStreamer {

    /**
     * Gets the ticket stream for a stream of queries.
     * @param query Query for which to generate a {@code Stream<Ticket>}.
     * @return stream of tickets
     */
    Stream<Ticket> getStream(TypedQuery<? extends Ticket> query);
}
