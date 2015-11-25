package org.jasig.cas.monitor;

import org.jasig.cas.mock.MockService;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.UniqueTicketIdGenerator;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link SessionMonitor} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class SessionMonitorTests {

    private static final ExpirationPolicy TEST_EXP_POLICY = new HardTimeoutExpirationPolicy(10000);
    private static final UniqueTicketIdGenerator GENERATOR = new DefaultUniqueTicketIdGenerator();

    private DefaultTicketRegistry defaultRegistry;
    private SessionMonitor monitor;

    @Before
    public void setUp() {
        this.defaultRegistry = new DefaultTicketRegistry();
        this.monitor = new SessionMonitor();
        this.monitor.setTicketRegistry(this.defaultRegistry);
    }

    @Test
    public void verifyObserveOk() throws Exception {
        addTicketsToRegistry(this.defaultRegistry, 5, 10);
        final SessionStatus status = this.monitor.observe();
        assertEquals(5, status.getSessionCount());
        assertEquals(10, status.getServiceTicketCount());
        assertEquals(StatusCode.OK, status.getCode());
    }

    @Test
    public void verifyObserveWarnSessionsExceeded() throws Exception {
        addTicketsToRegistry(this.defaultRegistry, 10, 1);
        this.monitor.setSessionCountWarnThreshold(5);
        final SessionStatus status = this.monitor.observe();
        assertEquals(StatusCode.WARN, status.getCode());
        assertTrue(status.getDescription().contains("Session count"));
    }

    @Test
    public void verifyObserveWarnServiceTicketsExceeded() throws Exception {
        addTicketsToRegistry(this.defaultRegistry, 1, 10);
        this.monitor.setServiceTicketCountWarnThreshold(5);
        final SessionStatus status = this.monitor.observe();
        assertEquals(StatusCode.WARN, status.getCode());
        assertTrue(status.getDescription().contains("Service ticket count"));
    }
    private void addTicketsToRegistry(final TicketRegistry registry, final int tgtCount, final int stCount) {
        TicketGrantingTicketImpl ticket = null;
        for (int i = 0; i < tgtCount; i++) {
            ticket = new TicketGrantingTicketImpl(
                    GENERATOR.getNewTicketId("TGT"),
                    org.jasig.cas.authentication.TestUtils.getAuthentication(),
                    TEST_EXP_POLICY);
            registry.addTicket(ticket);
        }

        if (ticket != null) {
          for (int i = 0; i < stCount; i++) {
              registry.addTicket(ticket.grantServiceTicket(
                      GENERATOR.getNewTicketId("ST"),
                      new MockService("junit"),
                      TEST_EXP_POLICY,
                      false,
                      true));
          }
        }
    }
}
