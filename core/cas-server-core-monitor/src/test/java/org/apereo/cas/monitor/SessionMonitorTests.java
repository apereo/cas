package org.apereo.cas.monitor;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;

import org.junit.Before;
import org.junit.Test;

import java.util.stream.IntStream;

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

    @Before
    public void setUp() {
        this.defaultRegistry = new DefaultTicketRegistry();
    }

    @Test
    public void verifyObserveOk() {
        addTicketsToRegistry(this.defaultRegistry, 5, 10);
        final SessionMonitor monitor = new SessionMonitor(defaultRegistry, -1, -1);
        final SessionStatus status = monitor.observe();
        assertEquals(5, status.getSessionCount());
        assertEquals(10, status.getServiceTicketCount());
        assertEquals(StatusCode.OK, status.getCode());
    }

    @Test
    public void verifyObserveWarnSessionsExceeded() {
        addTicketsToRegistry(this.defaultRegistry, 10, 1);
        final SessionMonitor monitor = new SessionMonitor(defaultRegistry, 0, 5);
        final SessionStatus status = monitor.observe();
        assertEquals(StatusCode.WARN, status.getCode());
        assertTrue(status.getDescription().contains("Session count"));
    }

    @Test
    public void verifyObserveWarnServiceTicketsExceeded() {
        addTicketsToRegistry(this.defaultRegistry, 1, 10);
        final SessionMonitor monitor = new SessionMonitor(defaultRegistry, 5, 0);
        final SessionStatus status = monitor.observe();
        assertEquals(StatusCode.WARN, status.getCode());
        assertTrue(status.getDescription().contains("Service ticket count"));
    }

    private static void addTicketsToRegistry(final TicketRegistry registry, final int tgtCount, final int stCount) {
        final TicketGrantingTicketImpl[] ticket = {null};
        IntStream.range(0, tgtCount).forEach(i -> {
            ticket[0] = new TicketGrantingTicketImpl(GENERATOR.getNewTicketId("TGT"), CoreAuthenticationTestUtils.getAuthentication(), TEST_EXP_POLICY);
            registry.addTicket(ticket[0]);
        });

        if (ticket[0] != null) {
            final Service testService = RegisteredServiceTestUtils.getService("junit");
            IntStream.range(0, stCount).forEach(i -> registry.addTicket(ticket[0].grantServiceTicket(GENERATOR.getNewTicketId("ST"),
                                    testService, TEST_EXP_POLICY, false, true)));
        }
    }
}
