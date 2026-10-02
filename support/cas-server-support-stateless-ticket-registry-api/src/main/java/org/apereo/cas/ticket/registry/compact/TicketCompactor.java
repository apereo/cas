package org.apereo.cas.ticket.registry.compact;

import module java.base;
import org.apereo.cas.ticket.EncodedTicket;
import org.apereo.cas.ticket.Ticket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Expand ticket.
     *
     * @param compactTicket the compact ticket
     * @return the ticket
     * @throws Throwable the throwable
     */
    Ticket expand(String compactTicket) throws Throwable;

    /**
     * Compact ticket. The creation and expiration times are always the first two fields.
     *
     * @param ticket the ticket
     * @return the compact ticket
     * @throws Exception the exception
     */
    default String compact(final Ticket ticket) throws Exception {
        val fields = new ArrayList<String>();
        fields.add(String.valueOf(ticket.getCreationTime().toEpochSecond()));
        fields.add(String.valueOf(ticket.getExpirationPolicy().toMaximumExpirationTime(ticket).toEpochSecond()));
        compactFields(fields, ticket);
        return CompactTicketCodec.encode(fields);
    }

    /**
     * Add the fields specific to this ticket type.
     *
     * @param fields the fields
     * @param ticket the ticket
     * @throws Exception the exception
     */
    @SuppressWarnings("UnusedVariable")
    default void compactFields(final List<String> fields, final Ticket ticket) throws Exception {
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
     * Whether the expanded ticket keeps the id it carries,
     * instead of the id it was looked up by.
     *
     * @return true or false
     */
    default boolean isTicketIdRetained() {
        return false;
    }

    /**
     * Parse the compact ticket and verify the number of fields.
     *
     * @param compactTicket the compact ticket
     * @param expectedCount the expected number of fields
     * @return the common ticket structure
     */
    default CompactTicket parse(final String compactTicket, final int expectedCount) {
        val ticketElements = CompactTicketCodec.decode(compactTicket);
        if (ticketElements.size() != expectedCount) {
            throw new IllegalArgumentException("Compact ticket has %s fields instead of %s"
                .formatted(ticketElements.size(), expectedCount));
        }
        val creationTime = Instant.ofEpochSecond(Long.parseLong(ticketElements.get(CompactTicketIndexes.CREATION_TIME.getIndex())));
        val expirationTime = Instant.ofEpochSecond(Long.parseLong(ticketElements.get(CompactTicketIndexes.EXPIRATION_TIME.getIndex())));
        return new CompactTicket(List.copyOf(ticketElements), creationTime, expirationTime);
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
        /**
         * Field at the given position.
         *
         * @param index the index
         * @return the field
         */
        public String get(final int index) {
            return ticketElements.get(index);
        }

        /**
         * Field at the given position.
         *
         * @param index the index
         * @return the field
         */
        public String get(final CompactTicketIndexes index) {
            return get(index.getIndex());
        }
    }
}
