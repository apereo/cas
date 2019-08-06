package org.apereo.cas.ticket.registry;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * This is {@link CachingTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
public class CachingTicketRegistry extends AbstractMapBasedTicketRegistry {

    private static final int INITIAL_CACHE_SIZE = 50;

    private static final long MAX_CACHE_SIZE = 100_000_000;

    private final Map<String, Ticket> mapInstance;

    private final LoadingCache<String, Ticket> storage;

    private final LogoutManager logoutManager;

    public CachingTicketRegistry(final LogoutManager logoutManager) {
        this(CipherExecutor.noOp(), logoutManager);
    }

    public CachingTicketRegistry(final CipherExecutor cipherExecutor, final LogoutManager logoutManager) {
        super(cipherExecutor);
        this.storage = Caffeine.newBuilder()
            .initialCapacity(INITIAL_CACHE_SIZE)
            .maximumSize(MAX_CACHE_SIZE)
            .expireAfter(new CachedTicketExpirationPolicy()).removalListener(new CachedTicketRemovalListener())
            .build(s -> {
                LOGGER.error("Load operation of the cache is not supported.");
                return null;
            });
        this.mapInstance = this.storage.asMap();
        this.logoutManager = logoutManager;
    }

    /**
     * The cached ticket expiration policy.
     */
    public static class CachedTicketExpirationPolicy implements Expiry<String, Ticket> {

        private static long getExpiration(final Ticket value, final long currentTime) {
            if (value.isExpired()) {
                LOGGER.debug("Ticket [{}] has expired and shall be evicted from the cache", value.getId());
                return 0;
            }
            return currentTime;
        }

        @Override
        public long expireAfterCreate(final String key, final Ticket value,
                                      final long currentTime) {
            return getExpiration(value, currentTime);
        }

        @Override
        public long expireAfterUpdate(final String key, final Ticket value,
                                      final long currentTime, final long currentDuration) {
            return getExpiration(value, currentDuration);
        }

        @Override
        public long expireAfterRead(final String key, final Ticket value,
                                    final long currentTime, final long currentDuration) {
            return getExpiration(value, currentDuration);
        }
    }

    /**
     * The cached ticket removal listener.
     */
    public class CachedTicketRemovalListener implements RemovalListener<String, Ticket> {

        @Override
        public void onRemoval(final String key, final Ticket value, final RemovalCause cause) {
            if (cause == RemovalCause.EXPIRED) {
                LOGGER.warn("Received removal notification for ticket [{}] with cause [{}]. Cleaning...", key, cause);
                if (value instanceof TicketGrantingTicket) {
                    logoutManager.performLogout(TicketGrantingTicket.class.cast(value));
                }
            }
        }
    }
}
