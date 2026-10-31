package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.support.events.logout.CasRequestSingleLogoutEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.lock.LockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jooq.lambda.Unchecked;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.support.TransactionOperations;

/**
 * This is {@link DefaultTicketRegistryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultTicketRegistryCleaner implements TicketRegistryCleaner {
    /*
     * Expired tickets are removed by this many workers at a time, each removal running in its own
     * transaction, so a backlog neither runs one ticket at a time nor drains the registry's connection pool.
     */
    private static final int MAXIMUM_CONCURRENT_REMOVALS = 8;

    private static final int MAXIMUM_EXPIRED_TICKETS_PER_SCAN = 1_000;

    /*
     * Every this many runs, starting with the first, the cleaner reads every ticket instead of only the expiry
     * candidates, which catches tickets that expire for reasons other than time and rows written before the
     * registry kept an expiration time. With the default one-minute schedule, that is at startup and hourly.
     */
    private static final int FULL_SCAN_INTERVAL = 60;

    private final LockRepository lockRepository;

    private final ConfigurableApplicationContext applicationContext;

    private final TicketRegistry ticketRegistry;

    private final TransactionOperations transactionOperations;

    private final AtomicLong runs = new AtomicLong();

    @Override
    public int clean() {
        try {
            if (!isCleanerSupported()) {
                LOGGER.trace("Ticket registry cleaner is not supported by [{}]", getClass().getSimpleName());
                return 0;
            }
            return cleanInternal();
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        return 0;
    }

    @Override
    public int cleanTicket(final Ticket ticket) {
        return lockRepository.execute(ticket.getId(), () -> {
            val clientInfo = ClientInfoHolder.getClientInfo();
            if (ticket instanceof final TicketGrantingTicket tgt) {
                applicationContext.publishEvent(new CasRequestSingleLogoutEvent(this, tgt, clientInfo));
            }

            try {
                LOGGER.debug("Cleaning up expired ticket [{}]", ticket.getId());
                val nb = ticketRegistry.deleteTicket(ticket);
                if (ticket instanceof final TicketGrantingTicket tgt) {
                    applicationContext.publishEvent(new CasTicketGrantingTicketDestroyedEvent(this, tgt, clientInfo));
                }
                return nb;
            } catch (final Throwable e) {
                LoggingUtils.error(LOGGER, e);
                return 0;
            }
        }).orElse(0);
    }

    /*
     * Expired tickets are collected and removed in batches, so a large backlog is never held in memory at once.
     * Another scan follows a full batch for as long as the previous one removed something; tickets that cannot be
     * removed are left for the next run instead of being collected over and over.
     */
    protected int cleanInternal() {
        val criteria = getScanCriteria();
        val threadFactory = Thread.ofVirtual().name("ticket-registry-cleaner-", 0).factory();
        try (val executor = Executors.newFixedThreadPool(MAXIMUM_CONCURRENT_REMOVALS, threadFactory)) {
            var ticketsDeleted = 0;
            var expiredTickets = findExpiredTickets(criteria);
            var removed = removeExpiredTickets(executor, expiredTickets);
            ticketsDeleted += removed;
            while (removed > 0 && expiredTickets.size() >= getMaximumExpiredTicketsPerScan()) {
                expiredTickets = findExpiredTickets(criteria);
                removed = removeExpiredTickets(executor, expiredTickets);
                ticketsDeleted += removed;
            }
            if (ticketsDeleted > 0) {
                LOGGER.info("[{}] expired tickets removed.", ticketsDeleted);
            }
            return ticketsDeleted;
        }
    }

    protected int getMaximumExpiredTicketsPerScan() {
        return MAXIMUM_EXPIRED_TICKETS_PER_SCAN;
    }

    /**
     * What this run reads. The first run and every sixtieth run after it read every ticket;
     * the others ask only for tickets whose stored expiration time has passed, which registries that keep such
     * a time can answer without reading the rest. Every ticket read is still checked for expiration.
     *
     * @return the criteria for this run
     */
    protected TicketRegistryStreamCriteria getScanCriteria() {
        val criteria = TicketRegistryStreamCriteria.builder().build();
        return runs.getAndIncrement() % FULL_SCAN_INTERVAL == 0
            ? criteria
            : criteria.withExpiringBefore(ZonedDateTime.now(Clock.systemUTC()));
    }

    private int removeExpiredTickets(final ExecutorService executor, final List<Ticket> expiredTickets) {
        val removals = expiredTickets
            .stream()
            .map(ticket -> executor.submit(() -> cleanTicket(ticket)))
            .toList();
        return removals
            .stream()
            .mapToInt(Unchecked.toIntFunction(Future::get))
            .sum();
    }

    /*
     * The scan is the only part that needs a transaction: streaming from a database keeps a cursor, a connection
     * and every row read so far attached to it. Expired tickets are collected and that transaction ends before
     * any of them is removed.
     */
    private List<Ticket> findExpiredTickets(final TicketRegistryStreamCriteria criteria) {
        val expiredTickets = transactionOperations.execute(_ -> streamExpiredTickets(criteria));
        return expiredTickets != null ? expiredTickets : List.of();
    }

    private List<Ticket> streamExpiredTickets(final TicketRegistryStreamCriteria criteria) {
        try (val ticketStream = ticketRegistry.stream(criteria)) {
            return ticketStream
                .filter(Objects::nonNull)
                .filter(Ticket::isExpired)
                .limit(getMaximumExpiredTicketsPerScan())
                .map(Ticket.class::cast)
                .toList();
        }
    }

    /**
     * Indicates whether the registry supports automated ticket cleanup.
     * Generally, a registry that is able to return a collection of available
     * tickets should be able to support the cleanup process. Default is {@code true}.
     *
     * @return true/false.
     */
    protected boolean isCleanerSupported() {
        return true;
    }
}
