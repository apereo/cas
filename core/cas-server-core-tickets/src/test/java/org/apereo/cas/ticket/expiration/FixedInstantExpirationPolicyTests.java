package org.apereo.cas.ticket.expiration;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.serialization.SerializationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.time.Clock;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FixedInstantExpirationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("ExpirationPolicy")
public class FixedInstantExpirationPolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "FixedInstantExpirationPolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).minimal(false).build().toObjectMapper();

    @Test
    void verifyPolicy() throws Throwable {
        val expirationPolicy = new FixedInstantExpirationPolicy(Instant.now(Clock.systemUTC()));
        val result = SerializationUtils.serialize(expirationPolicy);
        val policyRead = SerializationUtils.deserialize(result, FixedInstantExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);

        MAPPER.writeValue(JSON_FILE, expirationPolicy);
        val readPolicy = MAPPER.readValue(JSON_FILE, FixedInstantExpirationPolicy.class);
        assertEquals(expirationPolicy, readPolicy);
        
        assertEquals(0, expirationPolicy.getTimeToIdle());
        assertNotEquals(0, expirationPolicy.getTimeToLive());

        val ticketGrantingTicket = new MockTicketGrantingTicket("casuser");
        assertNotNull(expirationPolicy.toMaximumExpirationTime(ticketGrantingTicket));
        expirationPolicy.setExpirationInstant(Instant.now(Clock.systemUTC()).minusSeconds(10));
        assertTrue(expirationPolicy.isExpired(ticketGrantingTicket));
    }
}
