package org.apereo.cas.ticket.registry;

import net.spy.memcached.MemcachedClientIF;
import org.apereo.cas.ticket.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Key-value ticket registry implementation that stores tickets in memcached keyed on the ticket ID.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.3
 */
public class MemCacheTicketRegistry extends AbstractTicketRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemCacheTicketRegistry.class);
    private static final String NO_MEMCACHED_CLIENT_IS_DEFINED = "No memcached client is defined.";

    /**
     * Memcached client.
     */
    private final MemcachedClientIF client;

    /**
     * Creates a new instance using the given memcached client instance, which is presumably configured via
     * {@code net.spy.memcached.spring.MemcachedClientFactoryBean}.
     *
     * @param client Memcached client.
     */
    public MemCacheTicketRegistry(final MemcachedClientIF client) {
        this.client = client;
    }

    @Override
    public Ticket updateTicket(final Ticket ticketToUpdate) {
        Assert.notNull(this.client, NO_MEMCACHED_CLIENT_IS_DEFINED);

        final Ticket ticket = encodeTicket(ticketToUpdate);
        LOGGER.debug("Updating ticket [{}]", ticket);
        try {
            if (!this.client.replace(ticket.getId(), getTimeout(ticketToUpdate), ticket).get()) {
                LOGGER.error("Failed to update [{}]", ticket);
                return null;
            }
        } catch (final InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for response to async replace operation for ticket [{}]. "
                    + "Cannot determine whether update was successful.", ticket);
        } catch (final Exception e) {
            LOGGER.error("Failed updating [{}]", ticket, e);
        }
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        Assert.notNull(this.client, NO_MEMCACHED_CLIENT_IS_DEFINED);
        try {
            final Ticket ticket = encodeTicket(ticketToAdd);
            LOGGER.debug("Adding ticket [{}]", ticket);
            final int timeout = getTimeout(ticketToAdd);
            if (!this.client.add(ticket.getId(), getTimeout(ticketToAdd), ticket).get()) {
                LOGGER.error("Failed to add [{}] without timeout [{}]", ticketToAdd, timeout);
            }
            // Sanity check to ensure ticket can retrieved
            if (this.client.get(ticket.getId()) == null) {
                LOGGER.warn("Ticket [{}] was added to memcached with timeout [{}], yet it cannot be retrieved. "
                        + "Ticket expiration policy may be too aggressive ?", ticketToAdd, timeout);
            }
        } catch (final InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for response to async add operation for ticket [{}]."
                    + "Cannot determine whether add was successful.", ticketToAdd);
        } catch (final Exception e) {
            LOGGER.error("Failed adding [{}]", ticketToAdd, e);
        }
    }

    @Override
    public long deleteAll() {
        LOGGER.debug("deleteAll() isn't supported. Returning empty list");
        return 0;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        Assert.notNull(this.client, NO_MEMCACHED_CLIENT_IS_DEFINED);
        try {
            if (this.client.delete(ticketId).get()) {
                LOGGER.debug("Removed ticket [{}] from the cache", ticketId);
            } else {
                LOGGER.info("Ticket [{}] not found or is already removed.", ticketId);
            }
        } catch (final Exception e) {
            LOGGER.error("Ticket not found or is already removed. Failed deleting [{}]", ticketId, e);
        }
        return true;
    }

    @Override
    public Ticket getTicket(final String ticketIdToGet) {
        Assert.notNull(this.client, NO_MEMCACHED_CLIENT_IS_DEFINED);

        final String ticketId = encodeTicketId(ticketIdToGet);
        try {
            final Ticket t = (Ticket) this.client.get(ticketId);
            if (t != null) {
                return decodeTicket(t);
            }
        } catch (final Exception e) {
            LOGGER.error("Failed fetching [{}] ", ticketId, e);
        }
        return null;
    }

    @Override
    public Collection<Ticket> getTickets() {
        LOGGER.debug("getTickets() isn't supported. Returning empty list");
        return new ArrayList<>();
    }

    /**
     * Destroy the client and shut down.
     */
    @PreDestroy
    public void destroy() {
        if (this.client == null) {
            return;
        }
        this.client.shutdown();
    }

    /**
     * If not time out value is specified, expire the ticket immediately.
     *
     * @param ticket the ticket
     * @return timeout in milliseconds.
     */
    private static int getTimeout(final Ticket ticket) {
        final int ttl = ticket.getExpirationPolicy().getTimeToLive().intValue();
        if (ttl == 0) {
            return 1;
        }
        return ttl;
    }
}
