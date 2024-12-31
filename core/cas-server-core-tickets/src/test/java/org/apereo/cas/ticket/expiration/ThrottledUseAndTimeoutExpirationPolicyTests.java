package org.apereo.cas.ticket.expiration;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.factory.BaseTicketFactoryTests;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link ThrottledUseAndTimeoutExpirationPolicy}.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("ExpirationPolicy")
@TestPropertySource(properties = "cas.ticket.tgt.core.only-track-most-recent-session=true")
class ThrottledUseAndTimeoutExpirationPolicyTests extends BaseTicketFactoryTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "throttleUseAndTimeoutExpirationPolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final long TIMEOUT = 2000;

    private ThrottledUseAndTimeoutExpirationPolicy expirationPolicy;

    private TicketGrantingTicketImpl ticket;

    @BeforeEach
    void initialize() {
        expirationPolicy = new ThrottledUseAndTimeoutExpirationPolicy();
        expirationPolicy.setTimeToKillInSeconds(TIMEOUT);
        expirationPolicy.setTimeInBetweenUsesInSeconds(TIMEOUT / 5);
        ticket = new TicketGrantingTicketImpl("test", CoreAuthenticationTestUtils
            .getAuthentication(), expirationPolicy);
    }

    @Test
    void verifyTicketIsNotExpired() {
        assertFalse(ticket.isExpired());
    }

    @Test
    void verifyTicketIsExpired() {
        expirationPolicy.setTimeToKillInSeconds(-TIMEOUT);
        assertTrue(ticket.isExpired());
    }

    @Test
    void verifyTicketUsedButWithTimeout() {
        ticket.grantServiceTicket("test", RegisteredServiceTestUtils.getService(), expirationPolicy, false,
            serviceTicketSessionTrackingPolicy);
        expirationPolicy.setTimeToKillInSeconds(TIMEOUT);
        expirationPolicy.setTimeInBetweenUsesInSeconds(-10);
        assertFalse(ticket.isExpired());
    }

    @Test
    void verifyThrottleNotTriggeredWithinOneSecond() {
        ticket.grantServiceTicket("test", RegisteredServiceTestUtils.getService(), expirationPolicy, false,
            serviceTicketSessionTrackingPolicy);
        val clock = Clock.fixed(ticket.getLastTimeUsed().toInstant().plusMillis(999), ZoneOffset.UTC);
        expirationPolicy.setClock(clock);
        assertFalse(ticket.isExpired());
    }

    @Test
    void verifyNotWaitingEnoughTime() {
        ticket.grantServiceTicket("test", RegisteredServiceTestUtils.getService(), expirationPolicy, false,
            serviceTicketSessionTrackingPolicy);
        val clock = Clock.fixed(ticket.getLastTimeUsed().toInstant().plusSeconds(1), ZoneOffset.UTC);
        expirationPolicy.setClock(clock);
        assertTrue(ticket.isExpired());
    }

    @Test
    void verifySerializeATimeoutExpirationPolicyToJson() throws IOException {
        MAPPER.writeValue(JSON_FILE, expirationPolicy);
        val policyRead = MAPPER.readValue(JSON_FILE, ThrottledUseAndTimeoutExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }

    @Test
    void verifySerialization() {
        val result = SerializationUtils.serialize(expirationPolicy);
        val policyRead = SerializationUtils.deserialize(result, ThrottledUseAndTimeoutExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }
}
