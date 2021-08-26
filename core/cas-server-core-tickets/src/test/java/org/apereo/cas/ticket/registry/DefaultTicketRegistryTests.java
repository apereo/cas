package org.apereo.cas.ticket.registry;

import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.cipher.DefaultTicketCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test case to test the DefaultTicketRegistry based on test cases to test all
 * Ticket Registries.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@SpringBootTest(classes = BaseTicketRegistryTests.SharedTestConfiguration.class)
@Tag("Tickets")
public class DefaultTicketRegistryTests extends BaseTicketRegistryTests {

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return new DefaultTicketRegistry(new ConcurrentHashMap<>(10, 10, 5), CipherExecutor.noOp());
    }

    @RepeatedTest(1)
    public void verifyCountsUnknown() {
        val registry = mock(DefaultTicketRegistry.class);
        when(registry.stream()).thenThrow(IllegalArgumentException.class);
        when(registry.sessionCount()).thenCallRealMethod();
        when(registry.serviceTicketCount()).thenCallRealMethod();
        assertEquals(Long.MIN_VALUE, registry.sessionCount());
        assertEquals(Long.MIN_VALUE, registry.serviceTicketCount());
    }

    @RepeatedTest(1)
    public void verifyCountForPrincipal() {
        val user = UUID.randomUUID().toString();
        val tgt = new MockTicketGrantingTicket(user);
        val st = new MockServiceTicket("ST-123456", RegisteredServiceTestUtils.getService(), tgt);
        val registry = getNewTicketRegistry();
        registry.addTicket(tgt);
        registry.addTicket(st);

        val count = registry.countSessionsFor(user);
        assertEquals(1, count);
    }


    @RepeatedTest(1)
    public void verifyEncodeFails() {
        val cipher = new DefaultTicketCipherExecutor(null, null,
            "AES", 512, 16, "webflow");
        val reg = new DefaultTicketRegistry(new ConcurrentHashMap<>(10, 10, 5), cipher);
        assertNull(reg.encodeTicket(null));
        assertNotNull(reg.decodeTicket(mock(Ticket.class)));
    }
}
