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
 * Test cases for {@link TicketGrantingTicketExpirationPolicy}.
 *
 * @author William G. Thompson, Jr.
 * @since 3.4.10
 */
@Tag("Tickets")
@TestPropertySource(properties = "cas.ticket.tgt.core.only-track-most-recent-session=true")
class TicketGrantingTicketExpirationPolicyTests extends BaseTicketFactoryTests {

    private static final long HARD_TIMEOUT = 200;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "ticketGrantingTicketExpirationPolicyTests.json");

    private static final long SLIDING_TIMEOUT = 50;

    private static final String TGT_ID = "test";

    private TicketGrantingTicketExpirationPolicy expirationPolicy;

    private TicketGrantingTicketImpl ticketGrantingTicket;

    @BeforeEach
    public void initialize() {
        expirationPolicy = new TicketGrantingTicketExpirationPolicy(HARD_TIMEOUT, SLIDING_TIMEOUT);
        ticketGrantingTicket = new TicketGrantingTicketImpl(TGT_ID, CoreAuthenticationTestUtils.getAuthentication(), expirationPolicy);
    }

    @Test
    void verifyTgtIsExpiredByHardTimeOut() throws Throwable {
        val creationTime = ticketGrantingTicket.getCreationTime();

        expirationPolicy.setClock(Clock.fixed(creationTime.plusSeconds(HARD_TIMEOUT).minusNanos(1).toInstant(), ZoneOffset.UTC));
        ticketGrantingTicket.grantServiceTicket(TGT_ID, RegisteredServiceTestUtils.getService(),
            expirationPolicy, false, serviceTicketSessionTrackingPolicy);
        assertFalse(ticketGrantingTicket.isExpired());

        expirationPolicy.setClock(Clock.fixed(creationTime.plusSeconds(HARD_TIMEOUT).plusNanos(1).toInstant(), ZoneOffset.UTC));
        assertTrue(ticketGrantingTicket.isExpired());
    }

    @Test
    void verifyTgtIsExpiredBySlidingWindow() throws Throwable {
        ticketGrantingTicket.grantServiceTicket(TGT_ID, RegisteredServiceTestUtils.getService(),
            expirationPolicy, false, serviceTicketSessionTrackingPolicy);

        expirationPolicy.setClock(Clock.fixed(ticketGrantingTicket.getLastTimeUsed().plusSeconds(SLIDING_TIMEOUT).minusNanos(1).toInstant(), ZoneOffset.UTC));
        assertFalse(ticketGrantingTicket.isExpired());

        expirationPolicy.setClock(Clock.fixed(ticketGrantingTicket.getLastTimeUsed().plusSeconds(SLIDING_TIMEOUT).plusNanos(1).toInstant(), ZoneOffset.UTC));
        assertTrue(ticketGrantingTicket.isExpired());
    }

    @Test
    void verifySerializeAnExpirationPolicyToJson() throws IOException {
        val policy = new TicketGrantingTicketExpirationPolicy(100, 100);
        MAPPER.writeValue(JSON_FILE, policy);
        val policyRead = MAPPER.readValue(JSON_FILE, TicketGrantingTicketExpirationPolicy.class);
        assertEquals(policy, policyRead);
    }

    @Test
    void verifySerialization() throws Throwable {
        val result = SerializationUtils.serialize(expirationPolicy);
        val policyRead = SerializationUtils.deserialize(result, TicketGrantingTicketExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }
}
