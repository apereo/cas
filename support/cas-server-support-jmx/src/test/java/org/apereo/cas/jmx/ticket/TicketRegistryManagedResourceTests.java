package org.apereo.cas.jmx.ticket;

import module java.base;
import org.apereo.cas.jmx.BaseCasJmxTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link TicketRegistryManagedResourceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseCasJmxTests.SharedTestConfiguration.class)
@Tag("JMX")
@ExtendWith(CasTestExtension.class)
class TicketRegistryManagedResourceTests {
    private final TicketRegistry registry = mock(TicketRegistry.class);

    private final TicketRegistryManagedResource resource = new TicketRegistryManagedResource(registry,
        new StaticListableBeanFactory().getBeanProvider(TicketRegistryCleaner.class));

    @Autowired
    @Qualifier("ticketRegistryManagedResource")
    private TicketRegistryManagedResource ticketRegistryManagedResource;

    @Test
    void verifyOperation() {
        assertNotNull(this.ticketRegistryManagedResource);
        assertNotNull(this.ticketRegistryManagedResource.getTickets());
    }

    @Test
    void verifyCountsAndStreamClosure() {
        val closed = new AtomicInteger();
        val ticket = mock(Ticket.class);
        when(ticket.getId()).thenReturn("TGT-example");
        when(registry.stream()).thenAnswer(_ -> Stream.of(ticket).onClose(closed::incrementAndGet));
        when(registry.sessionCount()).thenReturn(1L);
        when(registry.serviceTicketCount()).thenReturn(Long.MIN_VALUE);

        assertEquals(Set.of("TGT-example"), resource.getTickets());
        assertEquals(1, resource.getTicketCount());
        assertEquals(1, resource.getSessionCount());
        assertEquals(Long.MIN_VALUE, resource.getServiceTicketCount());
        assertEquals(2, closed.get());
    }

    @Test
    void verifyBoundedExactPrefixQuery() {
        val closed = new AtomicInteger();
        val serviceTicket = mock(Ticket.class);
        when(serviceTicket.getPrefix()).thenReturn("ST");
        val session = mock(Ticket.class);
        when(session.getPrefix()).thenReturn("TGT");
        when(session.getId()).thenReturn("TGT-example");
        val unused = mock(Ticket.class);
        when(registry.stream()).thenAnswer(_ -> Stream.of(serviceTicket, session, unused).onClose(closed::incrementAndGet));

        assertArrayEquals(new String[]{"TGT-example"}, resource.getTicketsByPrefix("TGT", 1));
        verifyNoInteractions(unused);
        assertArrayEquals(ArrayUtils.EMPTY_STRING_ARRAY, resource.getTicketsByPrefix("TG", 10));
        assertEquals(2, closed.get());
    }

    @Test
    void verifyPrincipalSessions() {
        val closed = new AtomicBoolean();
        val session = mock(Ticket.class);
        when(session.getId()).thenReturn("TGT-example");
        val unused = mock(Ticket.class);
        when(registry.getSessionsFor("casuser")).thenAnswer(_ -> Stream.of(session, unused).onClose(() -> closed.set(true)));

        assertArrayEquals(new String[]{"TGT-example"}, resource.getSessionsFor("casuser", 1));
        verifyNoInteractions(unused);
        verify(registry).getSessionsFor("casuser");
        assertTrue(closed.get());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 1_001})
    void verifyInvalidLimits(final int limit) {
        assertThrows(IllegalArgumentException.class, () -> resource.getTicketsByPrefix("TGT", limit));
        assertThrows(IllegalArgumentException.class, () -> resource.getSessionsFor("casuser", limit));
        verifyNoInteractions(registry);
    }

    @Test
    void verifyBlankFilters() {
        assertThrows(IllegalArgumentException.class, () -> resource.getTicketsByPrefix(" ", 1));
        assertThrows(IllegalArgumentException.class, () -> resource.getSessionsFor(" ", 1));
        verifyNoInteractions(registry);
    }

    @Test
    void verifyStreamClosedOnFailure() {
        val closed = new AtomicBoolean();
        val ticket = mock(Ticket.class);
        when(ticket.getPrefix()).thenThrow(new IllegalStateException("Unavailable"));
        when(registry.stream()).thenAnswer(_ -> Stream.of(ticket).onClose(() -> closed.set(true)));

        assertThrows(IllegalStateException.class, () -> resource.getTicketsByPrefix("TGT", 1));
        assertTrue(closed.get());
    }

    @Test
    void verifyCleanup() {
        val cleaner = mock(TicketRegistryCleaner.class);
        when(cleaner.clean()).thenReturn(5);
        val beans = new StaticListableBeanFactory(Map.of("cleaner", cleaner));
        val managedResource = new TicketRegistryManagedResource(registry, beans.getBeanProvider(TicketRegistryCleaner.class));

        assertEquals(5, managedResource.clean());
        verify(cleaner).clean();
        verifyNoInteractions(registry);
    }

    @Test
    void verifyCleanupUnavailable() {
        assertThrows(IllegalStateException.class, resource::clean);
        verifyNoInteractions(registry);
    }
}
