package org.apereo.cas.ticket.registry;

import module java.base;
import lombok.Builder;
import lombok.Getter;
import lombok.SuperBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link HazelcastTicketDocument}, the entry the Hazelcast ticket registry keeps for a ticket.
 * It is stored with Hazelcast compact serialization ({@link HazelcastTicketDocumentSerializer}), so queries
 * read the principal, service and attributes without deserializing the ticket, which is kept as the bytes
 * of the Java-serialized (and, with encryption, encoded) ticket. Attributes are kept only for ticket-granting
 * tickets, as {@code key + separator + value} pairs, the only tickets searched by attribute.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Getter
@SuperBuilder
public class HazelcastTicketDocument {
    /**
     * Separator between an attribute name and one of its values in {@link #getAttributes()}.
     */
    public static final String ATTRIBUTE_VALUE_SEPARATOR = "\u001F";

    @Nullable
    private final String principal;

    @Nullable
    private final String service;

    private final byte[] ticket;

    @Builder.Default
    private final String[] attributes = ArrayUtils.EMPTY_STRING_ARRAY;

    /**
     * Attribute entry as stored in {@link #getAttributes()}.
     *
     * @param name  the attribute name
     * @param value the attribute value
     * @return the entry
     */
    public static String toAttributeEntry(final String name, final String value) {
        return name + ATTRIBUTE_VALUE_SEPARATOR + value;
    }
}
