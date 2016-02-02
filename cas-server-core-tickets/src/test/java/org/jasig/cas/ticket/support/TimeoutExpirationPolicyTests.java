package org.jasig.cas.ticket.support;

import org.jasig.cas.util.AuthTestUtils;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class TimeoutExpirationPolicyTests {

    private static final long TIMEOUT = 1;

    private ExpirationPolicy expirationPolicy;

    private Ticket ticket;

    @Before
    public void setUp() throws Exception {
        this.expirationPolicy = new TimeoutExpirationPolicy(TIMEOUT);

        this.ticket = new TicketGrantingTicketImpl("test", AuthTestUtils
            .getAuthentication(), this.expirationPolicy);

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
    public void verifyTicketIsExpired() throws InterruptedException {
        ticket = new TicketGrantingTicketImpl("test", AuthTestUtils.getAuthentication(), new TimeoutExpirationPolicy(-100));
        assertTrue(ticket.isExpired());
    }
}
