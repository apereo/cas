package org.apereo.cas.ticket.expiration;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Clock;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link HardTimeoutExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("ExpirationPolicy")
class HardTimeoutExpirationPolicyTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final long TIMEOUT = 10;

    private HardTimeoutExpirationPolicy expirationPolicy;

    private TicketGrantingTicket ticket;

    @BeforeEach
    void initialize() {
        expirationPolicy = new HardTimeoutExpirationPolicy(TIMEOUT);
        ticket = new TicketGrantingTicketImpl("test", CoreAuthenticationTestUtils
            .getAuthentication(), expirationPolicy);
    }

    @Test
    void verifyTicketIsNull() {
        assertTrue(expirationPolicy.isExpired(null));
    }

    @Test
    void verifyTicketIsNotExpired() {
        expirationPolicy.setClock(Clock.fixed(ticket.getCreationTime().toInstant().plusSeconds(TIMEOUT).minusNanos(1), ZoneOffset.UTC));
        assertFalse(ticket.isExpired());
    }

    @Test
    void verifyTicketIsExpired() {
        expirationPolicy.setClock(Clock.fixed(ticket.getCreationTime().toInstant().plusSeconds(TIMEOUT).plusNanos(1), ZoneOffset.UTC));
        assertTrue(ticket.isExpired());
    }

    @Test
    void verifySerializeANeverExpiresExpirationPolicyToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val policyWritten = new HardTimeoutExpirationPolicy();
        MAPPER.writeValue(jsonFile, policyWritten);
        val policyRead = MAPPER.readValue(jsonFile, HardTimeoutExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    void verifySerialization() {
        val policyWritten = new HardTimeoutExpirationPolicy();
        val result = SerializationUtils.serialize(policyWritten);
        val policyRead = SerializationUtils.deserialize(result, HardTimeoutExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
