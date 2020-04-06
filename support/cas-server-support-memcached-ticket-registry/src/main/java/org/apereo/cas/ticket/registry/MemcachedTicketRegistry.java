package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.spy.memcached.MemcachedClientIF;
import org.apache.commons.pool2.ObjectPool;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * Key-value ticket registry implementation that stores tickets in memcached keyed on the ticket ID.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.3
 */
@SuppressWarnings("FutureReturnValueIgnored")
@Slf4j
@RequiredArgsConstructor
public class MemcachedTicketRegistry extends AbstractTicketRegistry implements DisposableBean {

    /**
     * Memcached client.
     */
    private final ObjectPool<MemcachedClientIF> connectionPool;

    /**
     * If not time out value is specified, expire the ticket immediately.
     *
     * @param ticket the ticket
     * @return timeout in milliseconds.
     */
    private static int getTimeout(final Ticket ticket) {
        val ttl = ticket.getExpirationPolicy().getTimeToLive().intValue();
        if (ttl == 0) {
            return 1;
        }
        return ttl;
    }

    @Override
    public Ticket updateTicket(final Ticket ticketToUpdate) {
        val ticket = encodeTicket(ticketToUpdate);
        LOGGER.debug("Updating ticket [{}]", ticket);
        val clientFromPool = getClientFromPool();
        try {
            clientFromPool.replace(ticket.getId(), getTimeout(ticketToUpdate), ticket);
        } catch (final Exception e) {
            LOGGER.error("Failed updating [{}]", ticket, e);
        } finally {
            returnClientToPool(clientFromPool);
        }
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        val clientFromPool = getClientFromPool();
        try {
            val ticket = encodeTicket(ticketToAdd);
            LOGGER.trace("Adding ticket [{}]", ticket);
            clientFromPool.set(ticket.getId(), getTimeout(ticketToAdd), ticket);
        } catch (final Exception e) {
            LOGGER.error("Failed adding [{}]", ticketToAdd, e);
        } finally {
            returnClientToPool(clientFromPool);
        }
    }

    @Override
    public long deleteAll() {
        LOGGER.debug("deleteAll() isn't supported. Returning empty list");
        return 0;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketIdToDelete) {
        val clientFromPool = getClientFromPool();
        val ticketId = encodeTicketId(ticketIdToDelete);
        try {
            clientFromPool.delete(ticketId);
        } catch (final Exception e) {
            LOGGER.error("Ticket not found or is already removed. Failed deleting [{}]", ticketId, e);
        } finally {
            returnClientToPool(clientFromPool);
        }
        return true;
    }

    @Override
    public Ticket getTicket(final String ticketIdToGet, final Predicate<Ticket> predicate) {
        val clientFromPool = getClientFromPool();
        val ticketId = encodeTicketId(ticketIdToGet);
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
            LOGGER.error("Failed fetching [{}] ", ticketId, e);
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

    /**
     * Destroy the client and shut down.
     */
    @Override
    public void destroy() {
        this.connectionPool.close();
    }

    @SneakyThrows
    private MemcachedClientIF getClientFromPool() {
        return this.connectionPool.borrowObject();
    }

    private void returnClientToPool(final MemcachedClientIF clientFromPool) {
        try {
            if (clientFromPool != null) {
                this.connectionPool.returnObject(clientFromPool);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
