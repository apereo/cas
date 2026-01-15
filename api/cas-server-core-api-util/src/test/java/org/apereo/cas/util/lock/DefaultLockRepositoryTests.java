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

        val threads = new ArrayList<Thread>();
        IntStream.range(0, 10).forEach(i -> {
            val thread = new Thread(Unchecked.runnable(() -> {
                Thread.sleep(250);
                repository.execute(lockKey, () -> {
                    container.values.get(lockKey).add(UUID.randomUUID().toString());
                    return null;
                });
            }));
            thread.setName("Thread-" + i);
            threads.add(thread);
            thread.start();
        });
        for (val thread : threads) {
            try {
                thread.join();
            } catch (final InterruptedException e) {
                fail(e);
            }
        }
        assertEquals(10, container.values.get(lockKey).size());
    }

    private static final class Container {
        private final Map<String, List<String>> values = new HashMap<>();
    }
}
