package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.events.logout.CasRequestSingleLogoutEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.DefaultTicketCatalog;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.lock.LockRepository;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultTicketRegistryCleanerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Tickets")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseTicketRegistryTests.SharedTestConfiguration.class)
class DefaultTicketRegistryCleanerTests {

    @Test
    void verifyAction() throws Throwable {
        val applicationContext = mock(ConfigurableApplicationContext.class);
        val ticketRegistry = newTicketRegistry();
        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.setExpirationPolicy(new HardTimeoutExpirationPolicy(1));
        ticketRegistry.addTicket(tgt);
        assertEquals(1, ticketRegistry.getTickets().size());
        val cleaner = new DefaultTicketRegistryCleaner(LockRepository.noOp(), applicationContext, ticketRegistry, TransactionOperations.withoutTransaction());
        tgt.markTicketExpired();
        cleaner.clean();
        assertEquals(0, ticketRegistry.sessionCount());
        verify(applicationContext).publishEvent(any(CasRequestSingleLogoutEvent.class));
        verify(applicationContext).publishEvent(any(CasTicketGrantingTicketDestroyedEvent.class));
    }

    @Test
    void verifyLargeBatch() throws Throwable {
        val applicationContext = mock(ConfigurableApplicationContext.class);
        val ticketRegistry = newTicketRegistry();
        for (var i = 0; i < 10_000; i++) {
            val tgt = new MockTicketGrantingTicket(i + "-" + UUID.randomUUID());
            tgt.setExpirationPolicy(new HardTimeoutExpirationPolicy(1));
            ticketRegistry.addTicket(tgt);
            tgt.markTicketExpired();
        }
        val cleaner = new DefaultTicketRegistryCleaner(LockRepository.noOp(), applicationContext, ticketRegistry, TransactionOperations.withoutTransaction());
        val cleaned = cleaner.clean();
        assertEquals(10_000, cleaned);
        assertEquals(0, ticketRegistry.sessionCount());
    }

    @Test
    void verifyCleanFail() {
        val applicationContext = mock(ConfigurableApplicationContext.class);
        val ticketRegistry = mock(TicketRegistry.class);
        when(ticketRegistry.stream()).thenThrow(IllegalArgumentException.class);
        val cleaner = new DefaultTicketRegistryCleaner(LockRepository.noOp(), applicationContext, ticketRegistry, TransactionOperations.withoutTransaction());
        assertEquals(0, cleaner.clean());
    }

    @Test
    void verifyStreamClosedOnClean() {
        val applicationContext = mock(ConfigurableApplicationContext.class);
        val ticketRegistry = mock(TicketRegistry.class);
        val closed = new AtomicBoolean(false);
        val stream = Stream.<Ticket>empty().onClose(() -> closed.set(true));
        doReturn(stream).when(ticketRegistry).stream();
        val cleaner = new DefaultTicketRegistryCleaner(LockRepository.noOp(), applicationContext, ticketRegistry, TransactionOperations.withoutTransaction());
        cleaner.clean();
        assertTrue(closed.get());
    }

    @Test
    void verifyNoCleaner() {
        val applicationContext = mock(ConfigurableApplicationContext.class);
        val ticketRegistry = newTicketRegistry();
        val cleaner = new DefaultTicketRegistryCleaner(LockRepository.noOp(), applicationContext, ticketRegistry, TransactionOperations.withoutTransaction()) {
            @Override
            protected boolean isCleanerSupported() {
                return false;
            }
        };
        assertEquals(0, cleaner.clean());
    }

    @Test
    void verifyTicketsRemovedConcurrentlyOutsideScan() throws Throwable {
        val scanning = new AtomicBoolean();
        val removedDuringScan = new AtomicBoolean();
        val running = new AtomicInteger();
        val maximumRunning = new AtomicInteger();
        val bothRunning = new CountDownLatch(2);
        val ticketRegistry = new DefaultTicketRegistry(mock(TicketSerializationManager.class),
            new DefaultTicketCatalog(), mock(ConfigurableApplicationContext.class)) {
            @Override
            public int deleteTicket(final Ticket ticket) throws Exception {
                removedDuringScan.compareAndSet(false, scanning.get());
                maximumRunning.accumulateAndGet(running.incrementAndGet(), Math::max);
                try {
                    bothRunning.countDown();
                    bothRunning.await(5, TimeUnit.SECONDS);
                    return super.deleteTicket(ticket);
                } finally {
                    running.decrementAndGet();
                }
            }
        };
        for (var i = 0; i < 50; i++) {
            val tgt = new MockTicketGrantingTicket(i + "-" + UUID.randomUUID());
            tgt.setExpirationPolicy(new HardTimeoutExpirationPolicy(1));
            ticketRegistry.addTicket(tgt);
            tgt.markTicketExpired();
        }
        val transactionOperations = new TransactionOperations() {
            @Override
            public <T> T execute(final TransactionCallback<T> action) {
                scanning.set(true);
                try {
                    return action.doInTransaction(new SimpleTransactionStatus());
                } finally {
                    scanning.set(false);
                }
            }
        };
        val cleaner = new DefaultTicketRegistryCleaner(LockRepository.noOp(),
            mock(ConfigurableApplicationContext.class), ticketRegistry, transactionOperations);
        assertEquals(50, cleaner.clean());
        assertEquals(0, ticketRegistry.sessionCount());
        assertFalse(removedDuringScan.get());
        assertTrue(maximumRunning.get() > 1);
        assertTrue(maximumRunning.get() <= 8);
    }

    @Test
    void verifyExpiredTicketsRemovedInBatches() throws Throwable {
        val scans = new AtomicInteger();
        val ticketRegistry = newScanCountingTicketRegistry(scans, false);
        addExpiredTickets(ticketRegistry, 10);
        val cleaner = newBatchingCleaner(ticketRegistry);
        assertEquals(10, cleaner.clean());
        assertEquals(4, scans.get());
        assertEquals(0, ticketRegistry.sessionCount());
    }

    @Test
    void verifyBatchesStopWhenNothingIsRemoved() throws Throwable {
        val scans = new AtomicInteger();
        val ticketRegistry = newScanCountingTicketRegistry(scans, true);
        addExpiredTickets(ticketRegistry, 10);
        val cleaner = newBatchingCleaner(ticketRegistry);
        assertEquals(0, cleaner.clean());
        assertEquals(1, scans.get());
    }

    private static DefaultTicketRegistry newScanCountingTicketRegistry(final AtomicInteger scans, final boolean failRemovals) {
        return new DefaultTicketRegistry(mock(TicketSerializationManager.class),
            new DefaultTicketCatalog(), mock(ConfigurableApplicationContext.class)) {
            @Override
            public Stream<? extends Ticket> stream(final TicketRegistryStreamCriteria criteria) {
                scans.incrementAndGet();
                return super.stream(criteria);
            }

            @Override
            public int deleteTicket(final Ticket ticket) throws Exception {
                return failRemovals ? 0 : super.deleteTicket(ticket);
            }
        };
    }

    private static DefaultTicketRegistryCleaner newBatchingCleaner(final TicketRegistry ticketRegistry) {
        return new DefaultTicketRegistryCleaner(LockRepository.noOp(), mock(ConfigurableApplicationContext.class),
            ticketRegistry, TransactionOperations.withoutTransaction()) {
            @Override
            protected int getMaximumExpiredTicketsPerScan() {
                return 3;
            }
        };
    }

    private static void addExpiredTickets(final TicketRegistry ticketRegistry, final int count) throws Throwable {
        for (var i = 0; i < count; i++) {
            val tgt = new MockTicketGrantingTicket(i + "-" + UUID.randomUUID());
            tgt.setExpirationPolicy(new HardTimeoutExpirationPolicy(1));
            ticketRegistry.addTicket(tgt);
            tgt.markTicketExpired();
        }
    }

    private static TicketRegistry newTicketRegistry() {
        return new DefaultTicketRegistry(
            mock(TicketSerializationManager.class),
            new DefaultTicketCatalog(),
            mock(ConfigurableApplicationContext.class));
    }

}
