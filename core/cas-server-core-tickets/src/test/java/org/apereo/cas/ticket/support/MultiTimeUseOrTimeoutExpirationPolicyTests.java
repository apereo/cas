package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class MultiTimeUseOrTimeoutExpirationPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "multiTimeUseOrTimeoutExpirationPolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final long TIMEOUT_SECONDS = 1;

    private static final int NUMBER_OF_USES = 5;

    private static final int TIMEOUT_BUFFER = 50;

    private ExpirationPolicy expirationPolicy;

    private TicketGrantingTicket ticket;

    @Before
    public void setUp() {
        this.expirationPolicy = new MultiTimeUseOrTimeoutExpirationPolicy(NUMBER_OF_USES, TIMEOUT_SECONDS);
        this.ticket = new TicketGrantingTicketImpl("test", CoreAuthenticationTestUtils.getAuthentication(), this.expirationPolicy);
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
    public void verifyTicketIsExpiredByTime() throws InterruptedException {
        Thread.sleep(TIMEOUT_SECONDS * 1000 + TIMEOUT_BUFFER);
        assertTrue(this.ticket.isExpired());
    }

    @Test
    public void verifyTicketIsExpiredByCount() {
        IntStream.range(0, NUMBER_OF_USES)
                .forEach(i -> this.ticket.grantServiceTicket("test", RegisteredServiceTestUtils.getService(),
                        new NeverExpiresExpirationPolicy(), false, true));
        assertTrue(this.ticket.isExpired());
    }

    @Test
    public void verifySerializeATimeoutExpirationPolicyToJson() throws IOException {
        MAPPER.writeValue(JSON_FILE, expirationPolicy);
        final ExpirationPolicy policyRead = MAPPER.readValue(JSON_FILE, MultiTimeUseOrTimeoutExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }
}
