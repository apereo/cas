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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.RegistryCleaner;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

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
 * @version $Revision$
 * @since 3.0
 * @see JpaLockingStrategy
 * @see NoOpLockingStrategy
 */
public final class DefaultTicketRegistryCleaner implements RegistryCleaner {

    /** The Commons Logging instance. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The instance of the TicketRegistry to clean. */
    @NotNull
    private TicketRegistry ticketRegistry;

    /** Execution locking strategy */
    @NotNull
    private LockingStrategy lock = new NoOpLockingStrategy();

    private boolean logUserOutOfServices = true;


    /**
     * @see org.jasig.cas.ticket.registry.RegistryCleaner#clean()
     */ 
    public void clean() {
        this.log.info("Beginning ticket cleanup.");
        this.log.debug("Attempting to acquire ticket cleanup lock.");
        if (!this.lock.acquire()) {
            this.log.info("Could not obtain lock.  Aborting cleanup.");
            return;
        }
        this.log.debug("Acquired lock.  Proceeding with cleanup.");
        try {
            final List<Ticket> ticketsToRemove = new ArrayList<Ticket>();
            final Collection<Ticket> ticketsInCache;
            ticketsInCache = this.ticketRegistry.getTickets();
            for (final Ticket ticket : ticketsInCache) {
                if (ticket.isExpired()) {
                    ticketsToRemove.add(ticket);
                }
            }

            this.log.info(ticketsToRemove.size() + " tickets found to be removed.");
            for (final Ticket ticket : ticketsToRemove) {
                // CAS-686: Expire TGT to trigger single sign-out
                if (this.logUserOutOfServices && ticket instanceof TicketGrantingTicket) {
                    ((TicketGrantingTicket) ticket).expire();
                }
                this.ticketRegistry.deleteTicket(ticket.getId());
            }
        } finally {
            this.log.debug("Releasing ticket cleanup lock.");
            this.lock.release();
        }

        this.log.info("Finished ticket cleanup.");
    }


    /**
     * @param ticketRegistry The ticketRegistry to set.
     */
    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }


    /**
     * @param  strategy  Ticket cleanup locking strategy.  An exclusive locking
     * strategy is preferable if not required for some ticket backing stores,
     * such as JPA, in a clustered CAS environment.  Use {@link JdbcLockingStrategy}
     * for {@link org.jasig.cas.ticket.registry.JpaTicketRegistry} in a clustered
     * CAS environment.
     */
    public void setLock(final LockingStrategy strategy) {
        this.lock = strategy;
    }

    /**
     * Whether to log users out of services when we remove an expired ticket.  The default is true. Set this to
     * false to disable.
     *
     * @param logUserOutOfServices whether to log the user out of services or not.
     */
    public void setLogUserOutOfServices(final boolean logUserOutOfServices) {
        this.logUserOutOfServices = logUserOutOfServices;
    }
}
