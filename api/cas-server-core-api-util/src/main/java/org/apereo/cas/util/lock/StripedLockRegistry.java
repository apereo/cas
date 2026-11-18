package org.apereo.cas.util.lock;

import module java.base;
import lombok.val;

/**
 * Hands out one of a fixed set of {@link ReentrantLock}s, chosen by the lock key's hash code masked with
 * the given mask. Equal keys always get the same lock, unrelated keys may share one, and the number of
 * locks stays bounded no matter how many keys are used.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public final class StripedLockRegistry implements Function<Object, Lock> {
    private final Lock[] locks;

    private final int mask;

    /**
     * Create the registry with {@code mask + 1} locks.
     *
     * @param mask the mask applied to the key's hash code; a power of two minus one
     */
    public StripedLockRegistry(final int mask) {
        if (mask < 0 || Integer.bitCount(mask + 1) != 1) {
            throw new IllegalArgumentException("Mask must be a power of 2 minus 1: " + mask);
        }
        this.mask = mask;
        val lockCount = mask + 1;
        this.locks = IntStream.range(0, lockCount).mapToObj(index -> new ReentrantLock()).toArray(Lock[]::new);
    }

    @Override
    public Lock apply(final Object lockKey) {
        return locks[Objects.requireNonNull(lockKey, "Lock key must not be null").hashCode() & mask];
    }
}
