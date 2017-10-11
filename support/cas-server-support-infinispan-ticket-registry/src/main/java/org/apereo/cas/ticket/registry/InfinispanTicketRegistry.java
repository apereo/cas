package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link InfinispanTicketRegistry}. Infinispan is a distributed in-memory
 * key/value data store with optional schema.
 * It offers advanced functionality such as transactions, events, querying and distributed processing.
 * See <a href="http://infinispan.org/features/">http://infinispan.org/features/</a> for more info.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class InfinispanTicketRegistry extends AbstractTicketRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfinispanTicketRegistry.class);

    private final Cache<String, Ticket> cache;

    /**
     * Instantiates a new Infinispan ticket registry.
     *
     * @param cache the cache
     */
    public InfinispanTicketRegistry(final Cache<String, Ticket> cache) {
        this.cache = cache;
        LOGGER.info("Setting up Infinispan Ticket Registry...");
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        final Ticket encodedTicket = encodeTicket(ticket);
        this.cache.put(encodedTicket.getId(), encodedTicket);
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        final Ticket ticket = encodeTicket(ticketToAdd);

        final long idleTime = ticketToAdd.getExpirationPolicy().getTimeToIdle() <= 0
                ? ticketToAdd.getExpirationPolicy().getTimeToLive()
                : ticketToAdd.getExpirationPolicy().getTimeToIdle();

        LOGGER.debug("Adding ticket [{}] to cache store to live [{}] seconds and stay idle for [{}]",
                ticketToAdd.getId(), ticketToAdd.getExpirationPolicy().getTimeToLive(), idleTime);

        this.cache.put(ticket.getId(), ticket,
                ticketToAdd.getExpirationPolicy().getTimeToLive(), TimeUnit.SECONDS,
                idleTime, TimeUnit.SECONDS);
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        final String encTicketId = encodeTicketId(ticketId);
        if (ticketId == null) {
            return null;
        }
        final Ticket result = decodeTicket(Ticket.class.cast(cache.get(encTicketId)));
        if (result != null && result.isExpired()) {
            LOGGER.debug("Ticket [{}] has expired and is now removed from the cache", result.getId());
            this.cache.remove(encTicketId);
            return null;
        }
        return result;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        this.cache.remove(encodeTicketId(ticketId));
        return true;
    }

    @Override
    public long deleteAll() {
        final int size = this.cache.size();
        this.cache.clear();
        return size;
    }

    /**
     * Retrieve all tickets from the registry.
     *
     * @return collection of tickets currently stored in the registry. Tickets
     * might or might not be valid i.e. expired.
     */
    @Override
    public Collection<Ticket> getTickets() {
        return decodeTickets(this.cache.values());
    }
}
