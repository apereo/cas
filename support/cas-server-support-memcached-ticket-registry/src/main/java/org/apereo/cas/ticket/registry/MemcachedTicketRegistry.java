package org.apereo.cas.ticket.registry;

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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

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
@SuppressWarnings("FutureReturnValueIgnored")
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
    public Ticket updateTicket(final Ticket ticketToUpdate) throws Exception {
        val ticket = encodeTicket(ticketToUpdate);
        LOGGER.debug("Updating ticket [{}]", ticket);
        val clientFromPool = getClientFromPool();
        try {
            clientFromPool.replace(ticket.getId(), getTimeout(ticketToUpdate), ticket);
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
            clientFromPool.set(ticket.getId(), getTimeout(ticketToAdd), ticket);
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
            clientFromPool.delete(ticketId);
        } catch (final Exception e) {
            LOGGER.error("Ticket not found or is already removed. Failed deleting [{}]", ticketId);
            LoggingUtils.error(LOGGER, e);
        } finally {
            returnClientToPool(clientFromPool);
        }
        return 1;
    }

    @Override
    public Ticket getTicket(final String ticketIdToGet, final Predicate<Ticket> predicate) {
        val clientFromPool = getClientFromPool();
        val ticketId = digestIdentifier(ticketIdToGet);
        try {
            val ticketFromCache = (Ticket) clientFromPool.get(ticketId);
            if (ticketFromCache != null) {
                val result = decodeTicket(ticketFromCache);
                if (predicate.test(result)) {
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
        return new ArrayList<>(0);
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

    private MemcachedClientIF getClientFromPool() {
        return FunctionUtils.doUnchecked(this.connectionPool::borrowObject);
    }

    private void returnClientToPool(final MemcachedClientIF clientFromPool) {
        try {
            if (clientFromPool != null) {
                this.connectionPool.returnObject(clientFromPool);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }
}
