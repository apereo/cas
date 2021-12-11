package org.apereo.cas;

import lombok.val;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Fireborn Z
 * @since 6.5.0
 */
@Tag("CAS")
@TestPropertySource(properties = {"cas.ticket.crypto.enabled=true", "cas.ticket.registry.in-memory.crypto.enabled=true"})
public class DefaultCentralAuthenticationServiceRegistryCryptoEnabledTests extends AbstractCentralAuthenticationServiceTests {

    @Test
    public void verifyGrantServiceTicketConcurrency() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), DefaultCentralAuthenticationServiceTests.getService());
        val ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        // grant service ticket concurrency
        val serviceTicketIds = new CopyOnWriteArrayList<>();
        Runnable runnable = () -> {
            for (int i = 0; i < 10; i++) {
                val serviceName = "testDefault" + "-" + Thread.currentThread().getName() + "-" + i;
                val mockService = RegisteredServiceTestUtils.getService(serviceName);
                val mockCtx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), mockService);
                val serviceTicketId = getCentralAuthenticationService().grantServiceTicket(ticketGrantingTicket.getId(), mockService, mockCtx);
                serviceTicketIds.add(serviceTicketId.getId());
            }
        };
        val threads = new ArrayList<Thread>();
        val REQUEST_IN_BROWSER_CONCURRENCY = 5;
        for (int i = 0; i < REQUEST_IN_BROWSER_CONCURRENCY; i++) {
            val thread = new Thread(runnable);
            thread.setName("Thread-grantServiceTicket-" + i);
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new AssertionError(e.getMessage(), e);
            }
        }
        val ticket = getCentralAuthenticationService().getTicket(ticketGrantingTicket.getId(), TicketGrantingTicket.class);
        assertEquals(serviceTicketIds.size(), ticket.getServices().size());
    }
}
