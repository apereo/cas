package org.apereo.cas.ticket.expiration;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
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
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Simple")
public class ThrottledUseAndTimeoutExpirationPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "throttleUseAndTimeoutExpirationPolicy.json");

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static final long TIMEOUT = 2000;

    private ThrottledUseAndTimeoutExpirationPolicy expirationPolicy;

    private TicketGrantingTicketImpl ticket;

    @BeforeEach
    public void initialize() {
        this.expirationPolicy = new ThrottledUseAndTimeoutExpirationPolicy();
        this.expirationPolicy.setTimeToKillInSeconds(TIMEOUT);
        this.expirationPolicy.setTimeInBetweenUsesInSeconds(TIMEOUT / 5);
        this.ticket = new TicketGrantingTicketImpl("test", CoreAuthenticationTestUtils
            .getAuthentication(), this.expirationPolicy);
    }

    @Test
    public void verifyTicketIsNotExpired() {
        assertFalse(this.ticket.isExpired());
    }

    @Test
    public void verifyTicketIsExpired() {
        expirationPolicy.setTimeToKillInSeconds(-TIMEOUT);
        assertTrue(this.ticket.isExpired());
    }

    @Test
    public void verifyTicketUsedButWithTimeout() {
        this.ticket.grantServiceTicket("test", RegisteredServiceTestUtils.getService(), this.expirationPolicy, false,
            true);
        expirationPolicy.setTimeToKillInSeconds(TIMEOUT);
        expirationPolicy.setTimeInBetweenUsesInSeconds(-10);
        assertFalse(this.ticket.isExpired());
    }

    @Test
    public void verifyThrottleNotTriggeredWithinOneSecond() {
        this.ticket.grantServiceTicket("test", RegisteredServiceTestUtils.getService(), this.expirationPolicy, false,
                true);
        val clock = Clock.fixed(this.ticket.getLastTimeUsed().toInstant().plusMillis(999), ZoneId.of("UTC"));
        expirationPolicy.setClock(clock);
        assertFalse(this.ticket.isExpired());
    }

    @Test
    public void verifyNotWaitingEnoughTime() {
        this.ticket.grantServiceTicket("test", RegisteredServiceTestUtils.getService(), this.expirationPolicy, false,
            true);
        val clock = Clock.fixed(this.ticket.getLastTimeUsed().toInstant().plusSeconds(1), ZoneId.of("UTC"));
        expirationPolicy.setClock(clock);
        assertTrue(this.ticket.isExpired());
    }

    @Test
    public void verifySerializeATimeoutExpirationPolicyToJson() throws IOException {
        MAPPER.writeValue(JSON_FILE, expirationPolicy);
        val policyRead = MAPPER.readValue(JSON_FILE, ThrottledUseAndTimeoutExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }

    @Test
    public void verifySerialization() {
        val result = SerializationUtils.serialize(expirationPolicy);
        val policyRead = SerializationUtils.deserialize(result, ThrottledUseAndTimeoutExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }
}
