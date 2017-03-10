package org.apereo.cas.ticket.support;

import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.junit.Before;
import org.junit.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;

/**
 * @author William G. Thompson, Jr.
 * @since 3.4.10
 */
public class TicketGrantingTicketExpirationPolicyTests {

    private static final long HARD_TIMEOUT = 2;

    private static final long SLIDING_TIMEOUT = 2;

    private static final long TIMEOUT_BUFFER = 2; // needs to be long enough for timeouts to be triggered

    private TicketGrantingTicketExpirationPolicy expirationPolicy;

    private TicketGrantingTicket ticketGrantingTicket;

    @Before
    public void setUp() throws Exception {
        this.expirationPolicy = new TicketGrantingTicketExpirationPolicy(HARD_TIMEOUT, SLIDING_TIMEOUT);
        this.ticketGrantingTicket = new TicketGrantingTicketImpl("test",
                TestUtils.getAuthentication(),
                this.expirationPolicy);
    }

    @Test
    public void verifyTgtIsExpiredByHardTimeOut() throws InterruptedException {
        // keep tgt alive via sliding window until within SLIDING_TIME / 2 of the HARD_TIMEOUT
        final ZonedDateTime creationTime = ticketGrantingTicket.getCreationTime();
         while (creationTime.plus(HARD_TIMEOUT - SLIDING_TIMEOUT / 2, ChronoUnit.SECONDS).isAfter(ZonedDateTime.now(ZoneOffset.UTC))) {
             ticketGrantingTicket.grantServiceTicket("test",
                     org.apereo.cas.services.TestUtils.getService(), expirationPolicy, false,
                     true);
             Thread.sleep((SLIDING_TIMEOUT - TIMEOUT_BUFFER) * 1_000);
             assertFalse(this.ticketGrantingTicket.isExpired());
         }

         // final sliding window extension past the HARD_TIMEOUT
         ticketGrantingTicket.grantServiceTicket("test",
                 org.apereo.cas.services.TestUtils.getService(), expirationPolicy, false,
                 true);
         Thread.sleep((SLIDING_TIMEOUT / 2 + TIMEOUT_BUFFER) * 1_000);
         assertTrue(ticketGrantingTicket.isExpired());

    }

    @Test
    public void verifyTgtIsExpiredBySlidingWindow() throws InterruptedException {
        ticketGrantingTicket.grantServiceTicket("test",
                org.apereo.cas.services.TestUtils.getService(), expirationPolicy, false,
                true);
        Thread.sleep((SLIDING_TIMEOUT - TIMEOUT_BUFFER) * 1_000);
        assertFalse(ticketGrantingTicket.isExpired());

        ticketGrantingTicket.grantServiceTicket("test",
                org.apereo.cas.services.TestUtils.getService(), expirationPolicy, false,
                true);
        Thread.sleep((SLIDING_TIMEOUT - TIMEOUT_BUFFER) * 1_000);
        assertFalse(ticketGrantingTicket.isExpired());

        ticketGrantingTicket.grantServiceTicket("test",
                org.apereo.cas.services.TestUtils.getService(), expirationPolicy, false,
                true);
        Thread.sleep((SLIDING_TIMEOUT + TIMEOUT_BUFFER) * 1_000);
        assertTrue(ticketGrantingTicket.isExpired());

    }

}
