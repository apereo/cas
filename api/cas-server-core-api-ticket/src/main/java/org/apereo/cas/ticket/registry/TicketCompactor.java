package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import lombok.val;

/**
 * This is {@link org.apereo.cas.ticket.registry.TicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface TicketCompactor<T extends Ticket> {
    /**
     * Expand ticket.
     *
     * @param ticketId the ticket id
     * @return the ticket
     * @throws Throwable the throwable
     */
    Ticket expand(String ticketId) throws Throwable;

    /**
     * Compact ticket.
     *
     * @param ticket the ticket
     * @return the string
     * @throws Exception the exception
     */
    default String compact(final Ticket ticket) throws Exception {
        val creationTime = ticket.getCreationTime().toEpochSecond();
        val expirationTime = ticket.getExpirationPolicy().toMaximumExpirationTime(ticket).toEpochSecond();
        val builder = new StringBuilder(String.format("%s,%s", creationTime, expirationTime));
        return compact(builder, ticket);
    }

    /**
     * Compact string from a builder.
     *
     * @param compactBuilder the compact builder
     * @param ticket         the ticket
     * @return the string
     * @throws Exception the exception
     */
    @SuppressWarnings("UnusedVariable")
    default String compact(final StringBuilder compactBuilder, final Ticket ticket) throws Exception {
        return compactBuilder.toString();
    }

    /**
     * Gets ticket type.
     *
     * @return the ticket type
     */
    Class<T> getTicketType();
}
