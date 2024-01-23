package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.util.StringUtils;
import java.time.Instant;
import java.util.List;

/**
 * This is {@link TicketCompactor}.
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
    default CompactTicket parse(final String ticketId) {
        val ticketElements = List.of(StringUtils.commaDelimitedListToStringArray(ticketId));
        val creationTimeInSeconds = Instant.ofEpochSecond(Long.parseLong(ticketElements.get(CompactTicketIndexes.CREATION_TIME.getIndex())));
        val expirationTimeInSeconds = Instant.ofEpochSecond(Long.parseLong(ticketElements.get(CompactTicketIndexes.EXPIRATION_TIME.getIndex())));
        return new CompactTicket(ticketElements, creationTimeInSeconds, expirationTimeInSeconds);
    }

    /**
     * Validate.
     *
     * @param finalTicketId the final ticket id
     */
    default void validate(final String finalTicketId) {
    }

    @RequiredArgsConstructor
    @Getter
    enum CompactTicketIndexes {
        /**
         * Represents the creation time of a compact ticket.
         * The value of this variable is an integer that represents a specific time
         * using a timestamp format.
         */
        CREATION_TIME(0),
        /**
         * Represents the expiration time of a compact ticket.
         * The value of this variable is an integer that represents a specific time
         * using a timestamp format.
         */
        EXPIRATION_TIME(1),
        /**
         * This constant represents the service value of a compact ticket.
         */
        SERVICE(2);
        private final int index;
    }

    record CompactTicket(List<String> ticketElements, Instant creationTime, Instant expirationTime) {
    }
}
