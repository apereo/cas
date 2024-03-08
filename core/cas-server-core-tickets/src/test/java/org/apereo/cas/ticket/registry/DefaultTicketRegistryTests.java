package org.apereo.cas.ticket.registry;

import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.DefaultTicketCatalog;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.cipher.DefaultTicketCipherExecutor;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test case to test the DefaultTicketRegistry based on test cases to test all
 * Ticket Registries.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Tickets")
class DefaultTicketRegistryTests extends BaseTicketRegistryTests {

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return new DefaultTicketRegistry(mock(TicketSerializationManager.class), new DefaultTicketCatalog());
    }

    @RepeatedTest(1)
    void verifyCountsUnknown() throws Throwable {
        val registry = mock(DefaultTicketRegistry.class);
        when(registry.stream()).thenThrow(IllegalArgumentException.class);
        when(registry.sessionCount()).thenCallRealMethod();
        when(registry.serviceTicketCount()).thenCallRealMethod();
        assertEquals(Long.MIN_VALUE, registry.sessionCount());
        assertEquals(Long.MIN_VALUE, registry.serviceTicketCount());
    }

    @RepeatedTest(1)
    void verifyRegistryQuery() throws Throwable {
        val user = UUID.randomUUID().toString();
        val tgt = new MockTicketGrantingTicket(user);
        val st = new MockServiceTicket("ST-123456", RegisteredServiceTestUtils.getService(), tgt);
        val registry = getNewTicketRegistry();
        registry.addTicket(tgt);
        registry.addTicket(st);

        val count = registry.countSessionsFor(user);
        assertEquals(1, count);
        assertNotEquals(0, registry.query(TicketRegistryQueryCriteria.builder()
            .type(TicketGrantingTicket.PREFIX).build()).size());
    }

    @RepeatedTest(2)
    void verifyCountForService() throws Throwable {
        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registry = getNewTicketRegistry();
        val user = UUID.randomUUID().toString();

        for (int i = 0; i < 5; i++) {
            val tgt = new MockTicketGrantingTicket(user);
            val st = tgt.grantServiceTicket(service, TicketTrackingPolicy.noOp());
            registry.addTicket(st);
            registry.updateTicket(tgt);
        }
        val count = registry.countTicketsFor(service);
        assertEquals(5, count);
    }


    @RepeatedTest(1)
    void verifyEncodeFails() throws Throwable {
        val cipher = new DefaultTicketCipherExecutor(null, null,
            "AES", 512, 16, "webflow");
        val reg = new DefaultTicketRegistry(cipher, mock(TicketSerializationManager.class), new DefaultTicketCatalog());
        assertNull(reg.encodeTicket(null));
        assertNotNull(reg.decodeTicket(mock(Ticket.class)));
    }
}
