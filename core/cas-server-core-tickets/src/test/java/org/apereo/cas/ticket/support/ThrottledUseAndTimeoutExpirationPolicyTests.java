package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class ThrottledUseAndTimeoutExpirationPolicyTests {

    private static final File JSON_FILE = new File("throttleUseAndTimeoutExpirationPolicy.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final long TIMEOUT = 2000;

    private static final long TIMEOUT_BUFFER = 10;

    private ThrottledUseAndTimeoutExpirationPolicy expirationPolicy;

    private TicketGrantingTicket ticket;

    @Before
    public void setUp() throws Exception {
        this.expirationPolicy = new ThrottledUseAndTimeoutExpirationPolicy();
        this.expirationPolicy.setTimeToKillInMilliSeconds(TIMEOUT);
        this.expirationPolicy.setTimeInBetweenUsesInMilliSeconds(TIMEOUT / 5);

        this.ticket = new TicketGrantingTicketImpl("test", TestUtils
            .getAuthentication(), this.expirationPolicy);

    }

    @Test
    public void verifyTicketIsNotExpired() {
        assertFalse(this.ticket.isExpired());
    }

    @Test
    public void verifyTicketIsExpired() throws InterruptedException {
        expirationPolicy.setTimeToKillInMilliSeconds(-TIMEOUT);
        assertTrue(this.ticket.isExpired());
    }

    @Test
    public void verifyTicketUsedButWithTimeout() throws InterruptedException {
        this.ticket.grantServiceTicket("test", org.apereo.cas.services.TestUtils.getService(), this.expirationPolicy, false,
                true);
        expirationPolicy.setTimeToKillInMilliSeconds(TIMEOUT);
        expirationPolicy.setTimeInBetweenUsesInMilliSeconds(-10);
        assertFalse(this.ticket.isExpired());
    }

    @Test
    public void verifyNotWaitingEnoughTime() {
        this.ticket.grantServiceTicket("test", org.apereo.cas.services.TestUtils.getService(), this.expirationPolicy, false,
                true);
        expirationPolicy.setTimeToKillInMilliSeconds(TIMEOUT);
        assertTrue(this.ticket.isExpired());
    }

    @Test
    public void verifySerializeATimeoutExpirationPolicyToJson() throws IOException {
        mapper.writeValue(JSON_FILE, expirationPolicy);

        final ExpirationPolicy policyRead = mapper.readValue(JSON_FILE, ThrottledUseAndTimeoutExpirationPolicy.class);

        assertEquals(expirationPolicy, policyRead);
    }
}
