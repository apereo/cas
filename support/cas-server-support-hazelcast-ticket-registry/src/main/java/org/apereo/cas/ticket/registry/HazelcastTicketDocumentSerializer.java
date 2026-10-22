package org.apereo.cas.ticket.registry;

import module java.base;
import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.NonNull;

/**
 * Compact serializer for {@link HazelcastTicketDocument}. Hazelcast reads the principal, service and
 * attribute fields of a compact entry for indexing and queries without deserializing the ticket bytes.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class HazelcastTicketDocumentSerializer implements CompactSerializer<HazelcastTicketDocument> {
    @Override
    public @NonNull HazelcastTicketDocument read(final @NonNull CompactReader reader) {
        return HazelcastTicketDocument.builder()
            .principal(reader.readString("principal"))
            .service(reader.readString("service"))
            .ticket(Objects.requireNonNullElse(reader.readArrayOfInt8("ticket"), ArrayUtils.EMPTY_BYTE_ARRAY))
            .attributes(Objects.requireNonNullElse(reader.readArrayOfString("attributes"), ArrayUtils.EMPTY_STRING_ARRAY))
            .build();
    }

    @Override
    public void write(final @NonNull CompactWriter writer, final @NonNull HazelcastTicketDocument document) {
        writer.writeString("principal", document.getPrincipal());
        writer.writeString("service", document.getService());
        writer.writeArrayOfInt8("ticket", document.getTicket());
        writer.writeArrayOfString("attributes", document.getAttributes());
    }

    @Override
    public @NonNull String getTypeName() {
        return HazelcastTicketDocument.class.getName();
    }

    @Override
    public @NonNull Class<HazelcastTicketDocument> getCompactClass() {
        return HazelcastTicketDocument.class;
    }
}
