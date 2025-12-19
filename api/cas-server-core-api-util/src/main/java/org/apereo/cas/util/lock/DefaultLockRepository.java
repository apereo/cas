package org.apereo.cas.util.lock;

import module java.base;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.integration.support.locks.LockRegistry;

/**
 * This is {@link DefaultLockRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class DefaultLockRepository implements LockRepository {
    private static final int LOCK_TIMEOUT_SECONDS = 3;

    private final LockRegistry lockRegistry;

    @Override
    public <T> Optional<T> execute(final Object lockKey, final Supplier<T> consumer) {
        return Unchecked.supplier(() -> {
            val lock = lockRegistry.obtain(lockKey);
            val lockFound = lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return Optional.of(lockFound)
                .filter(Boolean::booleanValue)
                .map(result -> {
                    try {
                        return consumer.get();
                    } finally {
                        lock.unlock();
                    }
                });
        }).get();
    }
}
