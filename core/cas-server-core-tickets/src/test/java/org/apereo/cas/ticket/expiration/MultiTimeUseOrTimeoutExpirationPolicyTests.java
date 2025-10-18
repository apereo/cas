package org.apereo.cas.ticket.expiration;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.factory.BaseTicketFactoryTests;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.serialization.SerializationUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link MultiTimeUseOrTimeoutExpirationPolicy}.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("ExpirationPolicy")
@TestPropertySource(properties = "cas.ticket.tgt.core.service-tracking-policy=MOST_RECENT")
class MultiTimeUseOrTimeoutExpirationPolicyTests extends BaseTicketFactoryTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final long TIMEOUT_SECONDS = 1;

    private static final int NUMBER_OF_USES = 5;

    private MultiTimeUseOrTimeoutExpirationPolicy expirationPolicy;

    private TicketGrantingTicketImpl ticket;

    
    @BeforeEach
    void initialize() {
        expirationPolicy = new MultiTimeUseOrTimeoutExpirationPolicy(NUMBER_OF_USES, TIMEOUT_SECONDS);
        ticket = new TicketGrantingTicketImpl("test", CoreAuthenticationTestUtils.getAuthentication(), expirationPolicy);
    }

    @Test
    void verifyTicketIsNull() {
        assertTrue(expirationPolicy.isExpired(null));
        assertNotNull(expirationPolicy.toMaximumExpirationTime(ticket));
    }

    @Test
    void verifyTicketIsNotExpired() {
        expirationPolicy.setClock(Clock.fixed(ticket.getCreationTime().toInstant().plusSeconds(TIMEOUT_SECONDS).minusNanos(1), ZoneOffset.UTC));
        assertFalse(ticket.isExpired());
    }

    @Test
    void verifyTicketIsExpiredByTime() {
        expirationPolicy.setClock(Clock.fixed(ticket.getCreationTime().toInstant().plusSeconds(TIMEOUT_SECONDS).plusNanos(1), ZoneOffset.UTC));
        assertTrue(ticket.isExpired());
    }

    @Test
    void verifyTicketIsExpiredByCount() {
        IntStream.range(0, NUMBER_OF_USES)
            .forEach(i -> ticket.grantServiceTicket("test", RegisteredServiceTestUtils.getService(),
                NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy));
        assertTrue(ticket.isExpired());
    }

    @Test
    void verifySerializeATimeoutExpirationPolicyToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        MAPPER.writeValue(jsonFile, expirationPolicy);
        val policyRead = MAPPER.readValue(jsonFile, MultiTimeUseOrTimeoutExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }

    @Test
    void verifySerialization() {
        val result = SerializationUtils.serialize(expirationPolicy);
        val policyRead = SerializationUtils.deserialize(result, MultiTimeUseOrTimeoutExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }
}
