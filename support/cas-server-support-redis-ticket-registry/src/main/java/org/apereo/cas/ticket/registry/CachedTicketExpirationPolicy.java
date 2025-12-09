package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.key.RedisKeyGenerator;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import java.time.Duration;

/**
 * This is {@link CachedTicketExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class CachedTicketExpirationPolicy implements Expiry<@NonNull String, @NonNull Ticket> {

    @Override
    public long expireAfterCreate(final String key, final Ticket ticket,
                                  final long currentTime) {
        if (ticket.isExpired()) {
            LOGGER.trace("Ticket [{}] has expired and shall be evicted from the cache", ticket.getId());
            return 0;
        }
        return Duration.ofSeconds(RedisKeyGenerator.getTicketExpirationInSeconds(ticket)).toNanos();
    }

    @Override
    public long expireAfterUpdate(final String key, final Ticket ticket,
                                  final long currentTime, final long currentDuration) {
        return Long.MAX_VALUE;
    }

    @Override
    public long expireAfterRead(final String key, final Ticket ticket,
                                final long currentTime, final long currentDuration) {
        return Long.MAX_VALUE;
    }
}
