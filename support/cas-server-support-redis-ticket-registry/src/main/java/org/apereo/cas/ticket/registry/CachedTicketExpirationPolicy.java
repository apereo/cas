package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;

import com.github.benmanes.caffeine.cache.Expiry;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * This is {@link CachedTicketExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class CachedTicketExpirationPolicy implements Expiry<String, Ticket> {

    @Override
    public long expireAfterCreate(final String key, final Ticket value,
                                  final long currentTime) {
        if (value.isExpired()) {
            LOGGER.trace("Ticket [{}] has expired and shall be evicted from the cache", value.getId());
            return 0;
        }
        return Duration.ofSeconds(RedisCompositeKey.getTimeout(value)).toNanos();
    }

    @Override
    public long expireAfterUpdate(final String key, final Ticket value,
                                  final long currentTime, final long currentDuration) {
        return Long.MAX_VALUE;
    }

    @Override
    public long expireAfterRead(final String key, final Ticket value,
                                final long currentTime, final long currentDuration) {
        return Long.MAX_VALUE;
    }
}
