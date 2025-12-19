package org.apereo.cas.ticket.expiration;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.serialization.SerializationUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FixedInstantExpirationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("ExpirationPolicy")
class FixedInstantExpirationPolicyTests {
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
        
        assertNotEquals(0, expirationPolicy.getTimeToLive());

        val ticketGrantingTicket = new MockTicketGrantingTicket("casuser");
        assertNotNull(expirationPolicy.toMaximumExpirationTime(ticketGrantingTicket));
        expirationPolicy.setExpirationInstant(Instant.now(Clock.systemUTC()).minusSeconds(10));
        assertTrue(expirationPolicy.isExpired(ticketGrantingTicket));
    }
}
