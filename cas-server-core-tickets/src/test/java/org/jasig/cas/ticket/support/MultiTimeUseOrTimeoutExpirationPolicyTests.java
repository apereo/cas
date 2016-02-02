package org.jasig.cas.ticket.support;

import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.util.AuthTestUtils;
import org.jasig.cas.util.ServicesTestUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class MultiTimeUseOrTimeoutExpirationPolicyTests {

    private static final long TIMEOUT_MILLISECONDS = 100L;

    private static final int NUMBER_OF_USES = 5;

    private static final int TIMEOUT_BUFFER = 50;

    private ExpirationPolicy expirationPolicy;

    private TicketGrantingTicket ticket;

    @Before
    public void setUp() throws Exception {
        this.expirationPolicy = new MultiTimeUseOrTimeoutExpirationPolicy(NUMBER_OF_USES, TIMEOUT_MILLISECONDS,
                TimeUnit.MILLISECONDS);

        this.ticket = new TicketGrantingTicketImpl("test",
                AuthTestUtils.getAuthentication(), this.expirationPolicy);

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
            Thread.sleep(TIMEOUT_MILLISECONDS + TIMEOUT_BUFFER);
            assertTrue(this.ticket.isExpired());
    }

    @Test
    public void verifyTicketIsExpiredByCount() {
        IntStream.range(0, NUMBER_OF_USES)
                .forEach(i -> this.ticket.grantServiceTicket("test", ServicesTestUtils.getService(),
                        new NeverExpiresExpirationPolicy(), false, true));
        assertTrue(this.ticket.isExpired());
    }
}
