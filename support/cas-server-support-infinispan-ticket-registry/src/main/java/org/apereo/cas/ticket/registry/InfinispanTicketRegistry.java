package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.infinispan.Cache;

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
@Slf4j
@RequiredArgsConstructor
public class InfinispanTicketRegistry extends AbstractTicketRegistry {
    private final Cache<String, Ticket> cache;

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        val encodedTicket = encodeTicket(ticket);
        this.cache.put(encodedTicket.getId(), encodedTicket);
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        val ticket = encodeTicket(ticketToAdd);

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
        val encTicketId = encodeTicketId(ticketId);
        if (ticketId == null) {
            return null;
        }
        val result = decodeTicket(Ticket.class.cast(cache.get(encTicketId)));
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
        val size = this.cache.size();
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
    public Collection<? extends Ticket> getTickets() {
        return decodeTickets(this.cache.values());
    }
}
