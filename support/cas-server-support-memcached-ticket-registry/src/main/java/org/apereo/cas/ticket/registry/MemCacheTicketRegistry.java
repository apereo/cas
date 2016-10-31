package org.apereo.cas.ticket.registry;

import net.spy.memcached.MemcachedClientIF;
import org.apereo.cas.ticket.Ticket;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.util.Collection;

/**
 * Key-value ticket registry implementation that stores tickets in memcached keyed on the ticket ID.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.3
 */
public class MemCacheTicketRegistry extends AbstractTicketRegistry {

    /**
     * Memcached client.
     */
    private MemcachedClientIF client;

    /**
     * Instantiates a new Mem cache ticket registry.
     */
    public MemCacheTicketRegistry() {
    }
    
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
    public void updateTicket(final Ticket ticketToUpdate) {
        if (this.client == null) {
            logger.debug("No memcached client is available in the configuration.");
            return;
        }

        final Ticket ticket = encodeTicket(ticketToUpdate);
        logger.debug("Updating ticket {}", ticket);
        try {
            if (!this.client.replace(ticket.getId(), getTimeout(ticketToUpdate), ticket).get()) {
                logger.error("Failed to update {}", ticket);
            }
        } catch (final InterruptedException e) {
            logger.warn("Interrupted while waiting for response to async replace operation for ticket {}. "
                    + "Cannot determine whether update was successful.", ticket);
        } catch (final Exception e) {
            logger.error("Failed updating {}", ticket, e);
        }
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        if (this.client == null) {
            logger.debug("No memcached client is found in the configuration.");
            return;
        }

        final Ticket ticket = encodeTicket(ticketToAdd);
        logger.debug("Adding ticket {}", ticket);
        try {
            if (!this.client.add(ticket.getId(), getTimeout(ticketToAdd), ticket).get()) {
                logger.error("Failed to add {}", ticket);
            }
        } catch (final InterruptedException e) {
            logger.warn("Interrupted while waiting for response to async add operation for ticket {}."
                    + "Cannot determine whether add was successful.", ticket);
        } catch (final Exception e) {
            logger.error("Failed adding {}", ticket, e);
        }
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        try {
            Assert.notNull(this.client, "No memcached client is defined.");
            if (this.client.delete(ticketId).get()) {
                logger.debug("Removed ticket {} from the cache", ticketId);
            } else {
                logger.info("Ticket {} not found or is already removed.", ticketId);
            }
        } catch (final Exception e) {
            logger.error("Ticket not found or is already removed. Failed deleting {}", ticketId, e);
        }
        return true;
    }

    @Override
    public Ticket getTicket(final String ticketIdToGet) {
        if (this.client == null) {
            logger.debug("No memcached client is configured.");
            return null;
        }

        final String ticketId = encodeTicketId(ticketIdToGet);
        try {
            final Ticket t = (Ticket) this.client.get(ticketId);
            if (t != null) {
                return decodeTicket(t);
            }
        } catch (final Exception e) {
            logger.error("Failed fetching {} ", ticketId, e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * This operation is not supported.
     *
     * @throws UnsupportedOperationException if you try and call this operation.
     */
    @Override
    public Collection<Ticket> getTickets() {
        throw new UnsupportedOperationException("getTickets() not supported.");
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
