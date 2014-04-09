/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.ticket.registry.support;

import java.util.Collection;

import org.jasig.cas.ticket.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default ticket registry cleaner scans the entire CAS ticket registry
 * for expired tickets and removes them.  This process is only required so that
 * the size of the ticket registry will not grow beyond a reasonable size.
 * The functionality of CAS is not dependent on a ticket being removed as soon
 * as it is expired.
 * <p><strong>NEW</strong> in 3.3.6:</p>
 * <p>
 * Locking strategies may be used to support high availability environments.
 * In a clustered CAS environment with several CAS nodes executing ticket
 * cleanup, it is desirable to execute cleanup from only one CAS node at a time.
 * This dramatically reduces the potential for deadlocks in
 * {@link org.jasig.cas.ticket.registry.JpaTicketRegistry}, for example.
 * By default this implementation uses {@link NoOpLockingStrategy} to preserve
 * the same semantics as previous versions, but {@link JpaLockingStrategy}
 * should be used with {@link org.jasig.cas.ticket.registry.JpaTicketRegistry}
 * in a clustered CAS environment.
 * </p>
 * <p>The following property is required.</p>
 * <ul>
 * <li>ticketRegistry - CAS ticket registry.</li>
 * </ul>
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0
 * @see JpaLockingStrategy
 * @see NoOpLockingStrategy
 */
public final class DefaultTicketRegistryCleaner extends AbstractTicketRegistryCleaner {

    /** The Commons Logging instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @see org.jasig.cas.ticket.registry.RegistryCleaner#clean()
     */
    public void clean() {
        logger.info("Beginning ticket cleanup.");
        logger.debug("Attempting to acquire ticket cleanup lock.");
        if (!lock.acquire()) {
            logger.info("Could not obtain lock.  Aborting cleanup.");
            return;
        }
        logger.debug("Acquired lock.  Proceeding with cleanup.");
        try {
            final Collection<Ticket> ticketsInCache = ticketRegistry.getTickets();
            registryCleanerHelper.deleteExpiredTickets(ticketRegistry, logoutManager, ticketsInCache, logUserOutOfServices);
        } finally {
            logger.debug("Releasing ticket cleanup lock.");
            lock.release();
        }

        logger.info("Finished ticket cleanup.");
    }

}
