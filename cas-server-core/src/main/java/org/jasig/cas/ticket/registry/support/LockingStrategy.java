/*
 * Copyright 2009 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket.registry.support;

/**
 * Strategy pattern for defining a locking strategy in support of exclusive
 * execution of some process.
 *
 * @author Marvin S. Addison
 * @version $Revision$
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
