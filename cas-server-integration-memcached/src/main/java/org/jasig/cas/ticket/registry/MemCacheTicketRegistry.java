package org.jasig.cas.ticket.registry;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.MemcachedClientIF;
import org.jasig.cas.ticket.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Key-value ticket registry implementation that stores tickets in memcached keyed on the ticket ID.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.3
 */
@RefreshScope
@Component("memcachedTicketRegistry")
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
     * Creates a new instance that stores tickets in the given memcached hosts.
     *
     * @param hostnames                   Array of memcached hosts where each element is of the form host:port.
     */
    @Autowired
    public MemCacheTicketRegistry(@Value("${memcached.servers:}")
                                  final String[] hostnames) {

        try {
            final List<String> hostNamesArray = Arrays.asList(hostnames);
            if (hostNamesArray.isEmpty()) {
                logger.debug("No memcached hosts are define. Client shall not be configured");
            } else {
                logger.info("Setting up Memcached Ticket Registry...");
                this.client = new MemcachedClient(AddrUtil.getAddresses(hostNamesArray));
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException("Invalid memcached host specification.", e);
        }

    }

    /**
     * Creates a new instance using the given memcached client instance, which is presumably configured via
     * {@code net.spy.memcached.spring.MemcachedClientFactoryBean}.
     *
     * @param client                      Memcached client.
     */
    public MemCacheTicketRegistry(final MemcachedClientIF client) {
        this.client = client;
    }

    @Override
    protected void updateTicket(final Ticket ticketToUpdate) {
        if (this.client == null) {
            logger.debug("No memcached client is available in the configuration.");
            return;
        }

        final Ticket ticket = encodeTicket(ticketToUpdate);
        logger.debug("Updating ticket {}", ticket);
        try {
            if (!this.client.replace(ticket.getId(),  getTimeout(ticket), ticket).get()) {
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
            if (!this.client.add(ticket.getId(), getTimeout(ticket), ticket).get()) {
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
            return this.client.delete(ticketId).get();
        } catch (final Exception e) {
            logger.error("Ticket not found or is already removed. Failed deleting {}", ticketId, e);
        }
        return false;
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
                final Ticket result = decodeTicket(t);
                return getProxiedTicketInstance(result);
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

    @Override
    protected boolean isCleanerSupported() {
        logger.info("{} does not support automatic ticket clean up processes", this.getClass().getName());
        return false;
    }

    @Override
    protected boolean needsCallback() {
        return true;
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
