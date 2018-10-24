package org.apereo.cas.monitor;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Unit test for {@link SessionMonitor} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class SessionHealthIndicatorTests {

    private static final ExpirationPolicy TEST_EXP_POLICY = new HardTimeoutExpirationPolicy(10000);
    private static final UniqueTicketIdGenerator GENERATOR = new DefaultUniqueTicketIdGenerator();

    private DefaultTicketRegistry defaultRegistry;

    private static void addTicketsToRegistry(final TicketRegistry registry, final int tgtCount, final int stCount) {
        final TicketGrantingTicketImpl[] ticket = {null};
        IntStream.range(0, tgtCount).forEach(i -> {
            ticket[0] = new TicketGrantingTicketImpl(GENERATOR.getNewTicketId("TGT"), CoreAuthenticationTestUtils.getAuthentication(), TEST_EXP_POLICY);
            registry.addTicket(ticket[0]);
        });

        if (ticket[0] != null) {
            val testService = getService("junit");
            IntStream.range(0, stCount).forEach(i -> registry.addTicket(ticket[0].grantServiceTicket(GENERATOR.getNewTicketId("ST"),
                testService, TEST_EXP_POLICY, false, true)));
        }
    }

    public static AbstractWebApplicationService getService(final String name) {
        val request = new MockHttpServletRequest();
        request.addParameter("service", name);
        return (AbstractWebApplicationService) new WebApplicationServiceFactory().createService(request);
    }

    @BeforeEach
    public void initialize() {
        this.defaultRegistry = new DefaultTicketRegistry();
    }

    @Test
    public void verifyObserveOk() {
        addTicketsToRegistry(this.defaultRegistry, 5, 10);
        val monitor = new SessionMonitor(defaultRegistry, -1, -1);
        val status = monitor.health();
        assertEquals(Status.UP, status.getStatus());
    }

    @Test
    public void verifyObserveWarnSessionsExceeded() {
        addTicketsToRegistry(this.defaultRegistry, 10, 1);
        val monitor = new SessionMonitor(defaultRegistry, 0, 5);
        val status = monitor.health();
        assertEquals("WARN", status.getStatus().getCode());
    }

    @Test
    public void verifyObserveWarnServiceTicketsExceeded() {
        addTicketsToRegistry(this.defaultRegistry, 1, 10);
        val monitor = new SessionMonitor(defaultRegistry, 5, 0);
        val status = monitor.health();
        assertEquals("WARN", status.getStatus().getCode());
    }
}
