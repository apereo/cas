package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.ticket.registry.compact.CompactTicketCodec;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CompactTicketCodecTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Tickets")
class CompactTicketCodecTests {

    @Test
    void verifyFieldsRoundTripWithoutEscaping() {
        val nested = CompactTicketCodec.encodeValues(List.of("h1", StringUtils.EMPTY, "h,2:3#4"));
        val fields = new ArrayList<>(List.of("1727000000", StringUtils.EMPTY, "https://app.example.org/app,1,x:y#z|w;v=u",
            "CN=Jane Doe,OU=Staff:Faculty,O=Example", "ümlaut €uro 😀", nested));
        fields.add(null);
        val encoded = CompactTicketCodec.encode(fields);
        assertTrue(encoded.startsWith(CompactTicketCodec.VERSION + ";"));
        val decoded = CompactTicketCodec.decode(encoded);
        assertEquals(fields.size(), decoded.size());
        assertEquals(fields.subList(0, 6), decoded.subList(0, 6));
        assertEquals(StringUtils.EMPTY, decoded.get(6));
        assertEquals(List.of("h1", StringUtils.EMPTY, "h,2:3#4"), CompactTicketCodec.decodeValues(decoded.get(5)));
        assertTrue(CompactTicketCodec.decode(CompactTicketCodec.encode(List.of())).isEmpty());
        assertTrue(CompactTicketCodec.decodeValues(StringUtils.EMPTY).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {StringUtils.EMPTY, "1", "2;1:a", "1;5:ab", "1;x:a", "1;:a", "1;1a", "1;-1:a", "1;9999999999:a", "1;+1:a"})
    void verifyMalformedValuesAreRejected(final String value) {
        assertThrows(IllegalArgumentException.class, () -> CompactTicketCodec.decode(value));
    }
}
