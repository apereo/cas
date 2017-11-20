package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.registry.support.LockingStrategy;

/**
 * This is {@link NoOpLockingStrategy} that does absolutely nothing
 * when it comes to locking operations.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class NoOpLockingStrategy implements LockingStrategy {
    @Override
    public boolean acquire() {
        return true;
    }

    /**
     * Does nothing.
     */
    @Override
    public void release() {
    }
}
