package org.apereo.cas.ticket.expiration;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
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
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("Simple")
public class HardTimeoutExpirationPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "hardTimeoutExpirationPolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
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
    public void verifyTicketIsNull() {
        assertTrue(this.expirationPolicy.isExpired(null));
    }

    @Test
    public void verifyTicketIsNotExpired() {
        this.expirationPolicy.setClock(Clock.fixed(this.ticket.getCreationTime().toInstant().plusSeconds(TIMEOUT).minusNanos(1), ZoneId.of("UTC")));
        assertFalse(this.ticket.isExpired());
    }

    @Test
    public void verifyTicketIsExpired() {
        this.expirationPolicy.setClock(Clock.fixed(this.ticket.getCreationTime().toInstant().plusSeconds(TIMEOUT).plusNanos(1), ZoneId.of("UTC")));
        assertTrue(this.ticket.isExpired());
    }

    @Test
    public void verifySerializeANeverExpiresExpirationPolicyToJson() throws IOException {
        val policyWritten = new HardTimeoutExpirationPolicy();
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, HardTimeoutExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    public void verifySerialization() {
        val policyWritten = new HardTimeoutExpirationPolicy();
        val result = SerializationUtils.serialize(policyWritten);
        val policyRead = SerializationUtils.deserialize(result, HardTimeoutExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
