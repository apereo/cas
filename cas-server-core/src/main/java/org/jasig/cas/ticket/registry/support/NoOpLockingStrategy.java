/*
 * Copyright 2009 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry.support;


/**
 * No-Op locking strategy that allows the use of {@link DefaultTicketRegistryCleaner}
 * in environments where exclusive access to the registry for cleaning is either
 * unnecessary or not possible.
 *
 * @author Marvin Addison
 * @version $Revision$
 * @since 3.3.6
 *
 */
public class NoOpLockingStrategy implements LockingStrategy {

    /**
     * @see org.jasig.cas.ticket.registry.support.LockingStrategy#acquire()
     */
    public boolean acquire() {
        return true;
    }

    /**
     * @see org.jasig.cas.ticket.registry.support.LockingStrategy#release()
     */
    public void release() {
        // Nothing to release
    }

}
