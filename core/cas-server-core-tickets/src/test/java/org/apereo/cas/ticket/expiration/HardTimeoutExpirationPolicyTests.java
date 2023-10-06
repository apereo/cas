package org.apereo.cas.ticket.expiration;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
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

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "hardTimeoutExpirationPolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final long TIMEOUT = 10;

    private HardTimeoutExpirationPolicy expirationPolicy;

    private TicketGrantingTicketImpl ticket;

    @BeforeEach
    public void initialize() {
        this.expirationPolicy = new HardTimeoutExpirationPolicy(TIMEOUT);
        this.ticket = new TicketGrantingTicketImpl("test", CoreAuthenticationTestUtils
            .getAuthentication(), this.expirationPolicy);
    }

    @Test
    void verifyTicketIsNull() throws Throwable {
        assertTrue(this.expirationPolicy.isExpired(null));
    }

    @Test
    void verifyTicketIsNotExpired() throws Throwable {
        this.expirationPolicy.setClock(Clock.fixed(this.ticket.getCreationTime().toInstant().plusSeconds(TIMEOUT).minusNanos(1), ZoneOffset.UTC));
        assertFalse(this.ticket.isExpired());
    }

    @Test
    void verifyTicketIsExpired() throws Throwable {
        this.expirationPolicy.setClock(Clock.fixed(this.ticket.getCreationTime().toInstant().plusSeconds(TIMEOUT).plusNanos(1), ZoneOffset.UTC));
        assertTrue(this.ticket.isExpired());
        assertEquals(0, this.expirationPolicy.getTimeToIdle());
    }

    @Test
    void verifySerializeANeverExpiresExpirationPolicyToJson() throws IOException {
        val policyWritten = new HardTimeoutExpirationPolicy();
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, HardTimeoutExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    void verifySerialization() throws Throwable {
        val policyWritten = new HardTimeoutExpirationPolicy();
        val result = SerializationUtils.serialize(policyWritten);
        val policyRead = SerializationUtils.deserialize(result, HardTimeoutExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
