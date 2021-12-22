package org.apereo.cas.util.lock;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.integration.support.locks.LockRegistry;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

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
    @SneakyThrows
    public <T> Optional<T> execute(final Object lockKey, final Supplier<T> consumer) {
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
    }
}
