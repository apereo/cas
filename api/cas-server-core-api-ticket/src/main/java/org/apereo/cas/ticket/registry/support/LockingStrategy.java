package org.apereo.cas.ticket.registry.support;

/**
 * Strategy pattern for defining a locking strategy in support of exclusive
 * execution of some process.
 *
 * @author Marvin S. Addison
 * @since 3.3.6
 *
 */
public interface LockingStrategy {

    /**
     * Attempt to acquire the lock.
     *
     * @return  True if lock was successfully acquired, false otherwise.
     */
    boolean acquire();


    /**
     * Release the lock if held.  If the lock is not held nothing is done.
     */
    void release();
}
