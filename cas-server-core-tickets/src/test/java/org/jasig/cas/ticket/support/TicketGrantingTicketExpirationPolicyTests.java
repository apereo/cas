package org.jasig.cas.ticket.support;

import static org.junit.Assert.*;

import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author William G. Thompson, Jr.

 * @since 3.4.10
 */
public class TicketGrantingTicketExpirationPolicyTests {

    private static final long HARD_TIMEOUT = 100L;

    private static final long SLIDING_TIMEOUT = 60L; 

    private static final long TIMEOUT_BUFFER = 20L; // needs to be long enough for timeouts to be triggered

    private TicketGrantingTicketExpirationPolicy expirationPolicy;

    private TicketGrantingTicket ticketGrantingTicket;

    @Before
    public void setUp() throws Exception {
        this.expirationPolicy = new TicketGrantingTicketExpirationPolicy(HARD_TIMEOUT, SLIDING_TIMEOUT,
            TimeUnit.MILLISECONDS);
        this.ticketGrantingTicket = new TicketGrantingTicketImpl("test",
                org.jasig.cas.authentication.TestUtils.getAuthentication(),
                this.expirationPolicy);
    }

    @Test
    public void verifyTgtIsExpiredByHardTimeOut() throws InterruptedException {
         // keep tgt alive via sliding window until within SLIDING_TIME / 2 of the HARD_TIMEOUT
         while (System.currentTimeMillis() - ticketGrantingTicket.getCreationTime()
                 < (HARD_TIMEOUT - SLIDING_TIMEOUT / 2)) {
             ticketGrantingTicket.grantServiceTicket("test",
                     org.jasig.cas.services.TestUtils.getService(), expirationPolicy, false,
                     true);
             Thread.sleep(SLIDING_TIMEOUT - TIMEOUT_BUFFER);
             assertFalse(this.ticketGrantingTicket.isExpired());
         }

         // final sliding window extension past the HARD_TIMEOUT
         ticketGrantingTicket.grantServiceTicket("test",
                 org.jasig.cas.services.TestUtils.getService(), expirationPolicy, false,
                 true);
         Thread.sleep(SLIDING_TIMEOUT / 2 + TIMEOUT_BUFFER);
         assertTrue(ticketGrantingTicket.isExpired());

    }

    @Test
    public void verifyTgtIsExpiredBySlidingWindow() throws InterruptedException {
        ticketGrantingTicket.grantServiceTicket("test",
                org.jasig.cas.services.TestUtils.getService(), expirationPolicy, false,
                true);
        Thread.sleep(SLIDING_TIMEOUT - TIMEOUT_BUFFER);
        assertFalse(ticketGrantingTicket.isExpired());

        ticketGrantingTicket.grantServiceTicket("test",
                org.jasig.cas.services.TestUtils.getService(), expirationPolicy, false,
                true);
        Thread.sleep(SLIDING_TIMEOUT - TIMEOUT_BUFFER);
        assertFalse(ticketGrantingTicket.isExpired());

        ticketGrantingTicket.grantServiceTicket("test",
                org.jasig.cas.services.TestUtils.getService(), expirationPolicy, false,
                true);
        Thread.sleep(SLIDING_TIMEOUT + TIMEOUT_BUFFER);
        assertTrue(ticketGrantingTicket.isExpired());

    }

}
