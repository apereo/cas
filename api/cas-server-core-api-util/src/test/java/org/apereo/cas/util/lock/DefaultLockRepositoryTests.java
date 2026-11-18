package org.apereo.cas.util.lock;

import module java.base;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultLockRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Utility")
class DefaultLockRepositoryTests {
    @Test
    void verifyNoOp() {
        val repository = LockRepository.noOp();
        val lockKey = UUID.randomUUID().toString();
        val result = repository.execute(lockKey, () -> lockKey);
        assertTrue(result.isPresent());
        assertEquals(result.get(), lockKey);
    }

    @Test
    void verifyDefault() {
        val repository = LockRepository.asDefault();
        val lockKey = UUID.randomUUID().toString();

        val container = new Container();
        container.values.put(lockKey, new ArrayList<>());

        val start = new CountDownLatch(1);
        val threads = new ArrayList<Thread>();
        IntStream.range(0, 10).forEach(i -> {
            val thread = new Thread(Unchecked.runnable(() -> {
                start.await();
                repository.execute(lockKey, () -> {
                    container.values.get(lockKey).add(UUID.randomUUID().toString());
                    return null;
                });
            }));
            thread.setName("Thread-" + i);
            threads.add(thread);
            thread.start();
        });
        start.countDown();
        for (val thread : threads) {
            try {
                thread.join();
            } catch (final InterruptedException e) {
                fail(e);
            }
        }
        assertEquals(10, container.values.get(lockKey).size());
    }

    @Test
    void verifyStripedLocksForKeys() {
        val registry = new StripedLockRegistry(LockRepository.DEFAULT_MASK_ARRAY_LENGTH);
        val lockKey = UUID.randomUUID().toString();
        assertSame(registry.apply(lockKey), registry.apply(String.valueOf(lockKey.toCharArray())));
        assertSame(registry.apply(0), registry.apply(LockRepository.DEFAULT_MASK_ARRAY_LENGTH + 1));
        assertNotSame(registry.apply(0), registry.apply(1));
    }

    @Test
    void verifyStripedLockMask() {
        assertThrows(IllegalArgumentException.class, () -> new StripedLockRegistry(1000));
        assertThrows(IllegalArgumentException.class, () -> new StripedLockRegistry(-1));
        val singleLock = new StripedLockRegistry(0);
        assertSame(singleLock.apply("first"), singleLock.apply("second"));
    }

    @Test
    void verifyPassThroughLock() throws Exception {
        val lock = PassThroughLock.INSTANCE;
        lock.lock();
        try {
            assertTrue(lock.tryLock());
            assertTrue(lock.tryLock(1, TimeUnit.SECONDS));
        } finally {
            lock.unlock();
        }
        assertThrows(UnsupportedOperationException.class, lock::newCondition);
    }

    private static final class Container {
        private final Map<String, List<String>> values = new HashMap<>();
    }
}
