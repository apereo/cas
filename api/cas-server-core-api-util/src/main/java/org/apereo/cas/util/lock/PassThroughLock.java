package org.apereo.cas.util.lock;

import module java.base;

/**
 * A {@link Lock} that never blocks: every attempt to acquire it succeeds and releasing it does nothing.
 * It keeps no state, so a single instance is shared.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public final class PassThroughLock implements Lock {
    /**
     * The shared instance.
     */
    public static final Lock INSTANCE = new PassThroughLock();

    private PassThroughLock() {
    }

    @Override
    public void lock() {
    }

    @Override
    public void lockInterruptibly() {
    }

    @Override
    public boolean tryLock() {
        return true;
    }

    @Override
    public boolean tryLock(final long time, final TimeUnit unit) {
        return true;
    }

    @Override
    public void unlock() {
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("Conditions are not supported by a pass-through lock");
    }
}
