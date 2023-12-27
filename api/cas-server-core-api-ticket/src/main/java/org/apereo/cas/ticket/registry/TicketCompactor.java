package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import lombok.val;
import org.springframework.util.StringUtils;
import java.time.Instant;
import java.util.List;

/**
 * This is {@link org.apereo.cas.ticket.registry.TicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface TicketCompactor<T extends Ticket> {
    /**
     * Delimiter character to separate fields in the compacted ticket.
     */
    String DELIMITER = ",";

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
        val builder = new StringBuilder(String.format("%s%s%s", creationTime, DELIMITER, expirationTime));
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

    /**
     * Parse common ticket structure.
     *
     * @param ticketId the ticket id
     * @return the common ticket structure
     */
    default SharedTicketStructure parse(final String ticketId) {
        val ticketElements = List.of(StringUtils.commaDelimitedListToStringArray(ticketId));
        val creationTimeInSeconds = Instant.ofEpochSecond(Long.parseLong(ticketElements.get(0)));
        val expirationTimeInSeconds = Instant.ofEpochSecond(Long.parseLong(ticketElements.get(1)));
        return new SharedTicketStructure(ticketElements, creationTimeInSeconds, expirationTimeInSeconds);
    }

    record SharedTicketStructure(List<String> ticketElements, Instant creationTime, Instant expirationTime) {
    }
}
