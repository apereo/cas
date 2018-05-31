package org.apereo.cas.ticket.registry;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.MemcachedClientIF;
import org.apache.commons.pool2.ObjectPool;
import org.apereo.cas.ticket.Ticket;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Key-value ticket registry implementation that stores tickets in memcached keyed on the ticket ID.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.3
 */
@SuppressWarnings("FutureReturnValueIgnored")
@Slf4j
@AllArgsConstructor
public class MemcachedTicketRegistry extends AbstractTicketRegistry implements DisposableBean {

    /**
     * Memcached client.
     */
    private final ObjectPool<MemcachedClientIF> connectionPool;

    @Override
    public Ticket updateTicket(final Ticket ticketToUpdate) {
        final var ticket = encodeTicket(ticketToUpdate);
        LOGGER.debug("Updating ticket [{}]", ticket);
        final var clientFromPool = getClientFromPool();
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
        final var clientFromPool = getClientFromPool();
        try {
            final var ticket = encodeTicket(ticketToAdd);
            LOGGER.debug("Adding ticket [{}]", ticket);
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
        final var clientFromPool = getClientFromPool();
        final var ticketId = encodeTicketId(ticketIdToDelete);
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
    public Ticket getTicket(final String ticketIdToGet) {
        final var clientFromPool = getClientFromPool();
        final var ticketId = encodeTicketId(ticketIdToGet);
        try {
            final var ticketFromCache = (Ticket) clientFromPool.get(ticketId);
            if (ticketFromCache != null) {
                final var result = decodeTicket(ticketFromCache);
                if (result != null && result.isExpired()) {
                    LOGGER.debug("Ticket [{}] has expired and is now removed from the memcached", result.getId());
                    deleteSingleTicket(ticketId);
                    return null;
                }
                return result;
            }
        } catch (final Exception e) {
            LOGGER.error("Failed fetching [{}] ", ticketId, e);
        } finally {
            returnClientToPool(clientFromPool);
        }
        return null;
    }

    @Override
    public Collection<Ticket> getTickets() {
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

    /**
     * If not time out value is specified, expire the ticket immediately.
     *
     * @param ticket the ticket
     * @return timeout in milliseconds.
     */
    private static int getTimeout(final Ticket ticket) {
        final var ttl = ticket.getExpirationPolicy().getTimeToLive().intValue();
        if (ttl == 0) {
            return 1;
        }
        return ttl;
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
