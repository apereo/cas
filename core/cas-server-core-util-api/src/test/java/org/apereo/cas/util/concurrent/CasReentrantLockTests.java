package org.apereo.cas.util.concurrent;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasReentrantLockTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Utility")
class CasReentrantLockTests {
    @Test
    void verifyReentrantExecution() {
        val lock = new CasReentrantLock();
        val result = new AtomicReference<String>();
        lock.execute(() -> {
            result.set(lock.execute(() -> "result"));
        });
        assertEquals("result", result.get());
    }

    @Test
    void verifyReleaseAfterFailure() throws Exception {
        val lock = new CasReentrantLock();
        val failure = new IllegalStateException("test failure");
        val supplier = (Supplier<String>) () -> {
            throw failure;
        };
        assertSame(failure, assertThrows(IllegalStateException.class, () -> lock.execute(supplier)));
        try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            val result = executor.submit(() -> lock.tryLock(() -> "released"));
            assertEquals("released", result.get(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void verifyConcurrentExecution() throws Exception {
        val lock = new CasReentrantLock();
        val count = new int[1];
        val barrier = new CyclicBarrier(20);
        try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            val tasks = new ArrayList<Future<?>>();
            for (var i = 0; i < 20; i++) {
                tasks.add(executor.submit(() -> {
                    for (var j = 0; j < 100; j++) {
                        barrier.await(5, TimeUnit.SECONDS);
                        lock.execute(() -> {
                            count[0]++;
                        });
                    }
                    return null;
                }));
            }
            for (val task : tasks) {
                task.get(5, TimeUnit.SECONDS);
            }
        }
        assertEquals(2000, count[0]);
    }
}
