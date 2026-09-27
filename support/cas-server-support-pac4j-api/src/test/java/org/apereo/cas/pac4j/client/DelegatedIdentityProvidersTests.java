package org.apereo.cas.pac4j.client;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.util.InitializableObject;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedIdentityProvidersTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Delegation")
class DelegatedIdentityProvidersTests {
    @Test
    void verifyInitializationWaitsForInitializationInProgress() throws Throwable {
        val initializing = new CountDownLatch(1);
        val release = new CountDownLatch(1);
        val client = new InitializableObject() {
            @Override
            protected void internalInit(final boolean forceReinit) {
                initializing.countDown();
                FunctionUtils.doUnchecked(_ -> release.await());
            }
        };
        val secondThread = new AtomicReference<Thread>();
        try (val executor = Executors.newFixedThreadPool(2)) {
            val first = executor.submit(() -> DelegatedIdentityProviders.initialize(client).isInitialized());
            assertTrue(initializing.await(30, TimeUnit.SECONDS));
            val second = executor.submit(() -> {
                secondThread.set(Thread.currentThread());
                return DelegatedIdentityProviders.initialize(client).isInitialized();
            });
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            while (!second.isDone() && System.nanoTime() < deadline
                && (secondThread.get() == null || secondThread.get().getState() != Thread.State.TIMED_WAITING)) {
                Thread.sleep(10);
            }
            release.countDown();
            assertTrue(first.get(30, TimeUnit.SECONDS));
            assertTrue(second.get(30, TimeUnit.SECONDS));
        }
    }
}
