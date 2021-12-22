package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests methods for {@link DefaultCentralAuthenticationService}
 * that verify concurrent behavior with crypto enabled.
 *
 * @author Fireborn Z
 * @since 6.5.0
 */
@Tag("CAS")
@TestPropertySource(properties = {"cas.ticket.crypto.enabled=true", "cas.ticket.registry.in-memory.crypto.enabled=true"})
public class DefaultCentralAuthenticationServiceCryptoTests extends AbstractCentralAuthenticationServiceTests {
    private static final int REQUEST_IN_BROWSER_CONCURRENCY = 5;

    private static final int TICKETS_PER_REQUEST = 10;

    @Test
    public void verifyGrantServiceTicketConcurrency() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(),
            RegisteredServiceTestUtils.getService());
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val serviceTicketIds = new CopyOnWriteArrayList<>();
        Runnable runnable = () -> {
            for (var i = 0; i < TICKETS_PER_REQUEST; i++) {
                val serviceName = "testDefault" + '-' + Thread.currentThread().getName() + '-' + i;
                val mockService = RegisteredServiceTestUtils.getService(serviceName);
                val mockCtx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), mockService);
                val serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), mockService, mockCtx);
                serviceTicketIds.add(serviceTicketId.getId());
            }
        };
        val threads = new ArrayList<Thread>();
        for (var i = 0; i < REQUEST_IN_BROWSER_CONCURRENCY; i++) {
            val thread = new Thread(runnable);
            thread.setName("Thread-grantServiceTicket-" + i);
            threads.add(thread);
            thread.start();
        }
        for (val thread : threads) {
            try {
                thread.join();
            } catch (final InterruptedException e) {
                fail(e);
            }
        }
        val ticket = getCentralAuthenticationService().getTicket(ticketGrantingTicket.getId(), TicketGrantingTicket.class);
        assertEquals(serviceTicketIds.size(), ticket.getServices().size());
    }
}
