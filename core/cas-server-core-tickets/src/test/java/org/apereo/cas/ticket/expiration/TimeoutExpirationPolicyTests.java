package org.apereo.cas.ticket.expiration;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link TimeoutExpirationPolicy}.
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("ExpirationPolicy")
public class TimeoutExpirationPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "timeoutExpirationPolicy.json");
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();
    private static final long TIMEOUT = 1;

    private ExpirationPolicy expirationPolicy;

    private Ticket ticket;

    @BeforeEach
    public void initialize() {
        this.expirationPolicy = new TimeoutExpirationPolicy(TIMEOUT);

        this.ticket = new TicketGrantingTicketImpl("test", CoreAuthenticationTestUtils
            .getAuthentication(), this.expirationPolicy);
    }

    @Test
    public void verifyTicketIsNull() {
        assertTrue(this.expirationPolicy.isExpired(null));
    }

    @Test
    public void verifyTicketIsNotExpired() {
        assertFalse(this.ticket.isExpired());
    }

    @Test
    public void verifyTicketIsExpired() {
        ticket = new TicketGrantingTicketImpl("test", CoreAuthenticationTestUtils.getAuthentication(), new TimeoutExpirationPolicy(-100));
        assertTrue(ticket.isExpired());
    }

    @Test
    public void verifySerialization() {
        val result = SerializationUtils.serialize(expirationPolicy);
        val policyRead = SerializationUtils.deserialize(result, TimeoutExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }

    @Test
    public void verifySerializeATimeoutExpirationPolicyToJson() throws IOException {
        MAPPER.writeValue(JSON_FILE, expirationPolicy);
        val policyRead = MAPPER.readValue(JSON_FILE, TimeoutExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }
}
