package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.infinispan.Cache;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

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
        val expirationPolicy = ticketToAdd.getExpirationPolicy();
        val idleTime = expirationPolicy.getTimeToIdle() <= 0
            ? expirationPolicy.getTimeToLive()
            : expirationPolicy.getTimeToIdle();

        val ttl = getTimeToLive(expirationPolicy);
        LOGGER.debug("Adding ticket [{}] to cache to live [{}] seconds and stay idle for [{}] seconds", ticketToAdd.getId(), ttl, idleTime);
        this.cache.put(ticket.getId(), ticket, ttl, TimeUnit.SECONDS, idleTime, TimeUnit.SECONDS);
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        val encTicketId = encodeTicketId(ticketId);
        if (ticketId == null) {
            return null;
        }
        val result = decodeTicket(Ticket.class.cast(cache.get(encTicketId)));
        if (predicate.test(result)) {
            return result;
        }
        return null;
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
    
    @Override
    public Collection<? extends Ticket> getTickets() {
        return decodeTickets(this.cache.values());
    }

    private static Long getTimeToLive(final ExpirationPolicy expirationPolicy) {
        val timeToLive = expirationPolicy.getTimeToLive();
        return Long.MAX_VALUE == timeToLive ? Long.valueOf(Integer.MAX_VALUE) : timeToLive;
    }
}
