package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.spy.memcached.MemcachedClientIF;
import org.apache.commons.pool2.ObjectPool;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Key-value ticket registry implementation that stores tickets in memcached keyed on the ticket ID.
 * <p>
 * If the number sent by a client is larger than {@link #THIRTY_DAYS_IN_SECONDS}, the expiration
 * time of the ticket will be set to {@link #THIRTY_DAYS_IN_SECONDS} itself.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.3
 * @deprecated Since 7.0.0
 */
@Slf4j
@Deprecated(since = "7.0.0")
public class MemcachedTicketRegistry extends AbstractTicketRegistry implements DisposableBean {
    private static final int THIRTY_DAYS_IN_SECONDS = 60 * 60 * 24 * 30;

    /**
     * Memcached client.
     */
    private final ObjectPool<MemcachedClientIF> connectionPool;

    public MemcachedTicketRegistry(final CipherExecutor cipherExecutor, final TicketSerializationManager ticketSerializationManager,
                                   final TicketCatalog ticketCatalog, final ConfigurableApplicationContext applicationContext,
                                   final ObjectPool<MemcachedClientIF> connectionPool) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext);
        this.connectionPool = connectionPool;
    }

    @Override
    public @Nullable Ticket updateTicket(final Ticket ticketToUpdate) throws Exception {
        val ticket = encodeTicket(ticketToUpdate);
        LOGGER.debug("Updating ticket [{}]", ticket);
        val clientFromPool = getClientFromPool();
        try {
            val ticketId = Objects.requireNonNull(ticket).getId();
            if (!awaitCompletion(clientFromPool.replace(ticketId, getTimeout(ticketToUpdate), ticket))) {
                LOGGER.warn("Memcached did not accept the update of ticket [{}]", ticketId);
            }
        } catch (final Exception e) {
            LOGGER.error("Failed updating [{}]", ticket);
            LoggingUtils.error(LOGGER, e);
        } finally {
            returnClientToPool(clientFromPool);
        }
        return ticket;
    }

    @Override
    public Ticket addSingleTicket(final Ticket ticketToAdd) {
        val clientFromPool = getClientFromPool();
        try {
            val ticket = encodeTicket(ticketToAdd);
            LOGGER.trace("Adding ticket [{}]", ticket);
            val ticketId = Objects.requireNonNull(ticket).getId();
            if (!awaitCompletion(clientFromPool.set(ticketId, getTimeout(ticketToAdd), ticket))) {
                LOGGER.warn("Memcached did not accept ticket [{}]", ticketId);
            }
        } catch (final Exception e) {
            LOGGER.error("Failed adding [{}]", ticketToAdd);
            LoggingUtils.error(LOGGER, e);
        } finally {
            returnClientToPool(clientFromPool);
        }
        return ticketToAdd;
    }

    @Override
    public long deleteAll() {
        LOGGER.debug("deleteAll() isn't supported");
        return 0;
    }

    @Override
    public long deleteSingleTicket(final Ticket ticketToDelete) {
        val clientFromPool = getClientFromPool();
        val ticketId = digestIdentifier(ticketToDelete.getId());
        try {
            if (!awaitCompletion(clientFromPool.delete(ticketId))) {
                LOGGER.debug("Memcached held no entry for ticket [{}] to remove", ticketId);
            }
        } catch (final Exception e) {
            LOGGER.error("Ticket not found or is already removed. Failed deleting [{}]", ticketId);
            LoggingUtils.error(LOGGER, e);
        } finally {
            returnClientToPool(clientFromPool);
        }
        return 1;
    }

    @Override
    public @Nullable Ticket getTicket(final String ticketIdToGet, final Predicate<Ticket> predicate) {
        val clientFromPool = getClientFromPool();
        val ticketId = digestIdentifier(ticketIdToGet);
        try {
            val ticketFromCache = (Ticket) clientFromPool.get(ticketId);
            if (ticketFromCache != null) {
                val result = decodeTicket(ticketFromCache);
                if (result != null && predicate.test(result)) {
                    return result;
                }
                return null;
            }
        } catch (final Exception e) {
            LOGGER.error("Failed fetching [{}] ", ticketId);
            LoggingUtils.error(LOGGER, e);
        } finally {
            returnClientToPool(clientFromPool);
        }
        return null;
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        LOGGER.debug("getTickets() isn't supported. Returning empty list");
        return new ArrayList<>();
    }

    @Override
    public void destroy() {
        this.connectionPool.close();
    }

    /**
     * If not time out value is specified, expire the ticket immediately.
     *
     * @param ticket the ticket
     * @return timeout in milliseconds.
     */
    private static int getTimeout(final Ticket ticket) {
        val timeToLive = ticket.getExpirationPolicy().getTimeToLive();
        var ttl = Long.MAX_VALUE == timeToLive ? Long.valueOf(Integer.MAX_VALUE) : timeToLive;
        if (ttl == 0) {
            return 1;
        }
        if (ttl >= THIRTY_DAYS_IN_SECONDS) {
            LOGGER.warn("Time-to-live value [{}] is greater than or equal to [{}]", ttl, THIRTY_DAYS_IN_SECONDS);
            return THIRTY_DAYS_IN_SECONDS;
        }
        return ttl.intValue();
    }

    /**
     * Memcached writes are queued on the client's I/O thread, so {@code set}, {@code replace} and
     * {@code delete} return before the server has seen the operation. A read that follows can then be
     * answered from before the write -- which is what CAS does whenever it issues a ticket and the
     * ticket is fetched immediately afterwards, and what a pooled client makes worse, since the read
     * may travel over a different connection than the write and carries no ordering against it.
     * <p>
     * Waiting on the operation is what orders the two. The wait is bounded by the client's own
     * configured operation timeout rather than by anything set here.
     *
     * @param operation the pending memcached operation
     * @return true if the server accepted it
     * @throws Exception if the wait is interrupted or the operation fails
     */
    private static boolean awaitCompletion(final Future<Boolean> operation) throws Exception {
        return operation != null && Boolean.TRUE.equals(operation.get());
    }

    private MemcachedClientIF getClientFromPool() {
        return FunctionUtils.doUnchecked(this.connectionPool::borrowObject);
    }

    private void returnClientToPool(final MemcachedClientIF clientFromPool) {
        try {
            connectionPool.returnObject(clientFromPool);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }
}
