package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.ticket.EncodedTicket;
import org.apereo.cas.ticket.Ticket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * This is {@link TicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface TicketCompactor<T extends Ticket> {
    /**
     * Logger instance.
     */
    Logger LOGGER = LoggerFactory.getLogger(TicketCompactor.class);


    /**
     * Delimiter character to separate fields in the compacted ticket.
     */
    String DELIMITER = ",";

    /**
     * Delimiter character to separate multiple values inside a single field.
     */
    String VALUE_DELIMITER = "#";

    /**
     * Encode a single value so it cannot contain any of the delimiters.
     *
     * @param value the value
     * @return the encoded value
     */
    static String encodeValue(final @Nullable String value) {
        return value == null ? "" : Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encode values into a single field.
     *
     * @param values the values
     * @return the encoded field
     */
    static String encodeValues(final Collection<?> values) {
        return values.stream().map(String::valueOf).map(TicketCompactor::encodeValue).collect(Collectors.joining(VALUE_DELIMITER));
    }

    /**
     * Decode a single value.
     *
     * @param value the value
     * @return the decoded value
     */
    static String decodeValue(final String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    /**
     * Decode a field into its values.
     *
     * @param values the encoded field
     * @return the decoded values
     */
    static List<String> decodeValues(final String values) {
        return Arrays.stream(values.split(VALUE_DELIMITER)).filter(value -> !value.isEmpty()).map(TicketCompactor::decodeValue).toList();
    }

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
     * Gets ticket length.
     *
     * @return the ticket length
     */
    default long getMaximumTicketLength() {
        return 0L;
    }

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
     * Parse common ticket structure and verify the number of fields.
     *
     * @param ticketId      the ticket id
     * @param expectedCount the expected number of fields
     * @return the common ticket structure
     */
    default CompactTicket parse(final String ticketId, final int expectedCount) {
        val structure = parse(ticketId);
        if (structure.ticketElements().size() != expectedCount) {
            throw new IllegalArgumentException("Compact ticket has %s fields instead of %s"
                .formatted(structure.ticketElements().size(), expectedCount));
        }
        return structure;
    }

    /**
     * Validate.
     *
     * @param finalTicket the final ticket
     * @return the ticket
     */
    default Ticket validate(final EncodedTicket finalTicket) {
        if (getMaximumTicketLength() > 0 && finalTicket.getId().length() >= getMaximumTicketLength()) {
            LOGGER.warn("Final ticket id [{}] length [{}] exceeds [{}] characters",
                finalTicket.getId(), finalTicket.getId().length(), getMaximumTicketLength());
        }
        return finalTicket;
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
