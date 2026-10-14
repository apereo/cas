package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.key.RedisKeyGenerator;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;

/**
 * This is {@link CachedTicketExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class CachedTicketExpirationPolicy implements Expiry<String, @NonNull Ticket> {
    /*
     * Another node announces changes to a ticket over Redis pub/sub, which drops messages a node misses while it is
     * disconnected. No copy is kept longer than this, so a missed change is picked up from Redis soon after.
     */
    static final Duration MAXIMUM_CACHE_DURATION = Duration.ofSeconds(30);

    @Override
    public long expireAfterCreate(final String key, final Ticket ticket,
                                  final long currentTime) {
        return getCacheDuration(ticket);
    }

    @Override
    public long expireAfterUpdate(final String key, final Ticket ticket,
                                  final long currentTime, final long currentDuration) {
        return getCacheDuration(ticket);
    }

    @Override
    public long expireAfterRead(final String key, final Ticket ticket,
                                final long currentTime, final long currentDuration) {
        return currentDuration;
    }

    private static long getCacheDuration(final Ticket ticket) {
        if (ticket.isExpired()) {
            LOGGER.trace("Ticket [{}] has expired and shall be evicted from the cache", ticket.getId());
            return 0;
        }
        val ticketDuration = Duration.ofSeconds(RedisKeyGenerator.getTicketExpirationInSeconds(ticket));
        return (ticketDuration.compareTo(MAXIMUM_CACHE_DURATION) < 0 ? ticketDuration : MAXIMUM_CACHE_DURATION).toNanos();
    }
}
