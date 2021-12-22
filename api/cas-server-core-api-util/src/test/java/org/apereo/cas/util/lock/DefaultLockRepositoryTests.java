package org.apereo.cas.util.lock;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultLockRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Utility")
public class DefaultLockRepositoryTests {
    @Test
    public void verifyNoOp() throws Exception {
        val repository = LockRepository.noOp();
        val lockKey = UUID.randomUUID().toString();
        val result = repository.execute(lockKey, () -> lockKey);
        assertTrue(result.isPresent());
        assertEquals(result.get(), lockKey);
    }

    @Test
    public void verifyDefault() throws Exception {
        val repository = LockRepository.asDefault();
        val lockKey = UUID.randomUUID().toString();

        val container = new Container();
        container.values.put(lockKey, new ArrayList<>());

        val threads = new ArrayList<Thread>();
        IntStream.range(0, 10).forEach(i -> {
            val thread = new Thread(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {
                    Thread.sleep(250);
                    repository.execute(lockKey, () -> {
                        container.values.get(lockKey).add(UUID.randomUUID().toString());
                        return null;
                    });
                }
            });
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

    private static class Container {
        private final Map<String, List<String>> values = new HashMap<>();
    }
}
