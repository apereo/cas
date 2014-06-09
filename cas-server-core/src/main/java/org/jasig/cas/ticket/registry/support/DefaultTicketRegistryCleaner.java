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

import org.jasig.cas.logout.LogoutManager;
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
 * @author Misagh Moayyed
 * @since 3.0
 * @see JpaLockingStrategy
 * @see NoOpLockingStrategy
 */
public final class DefaultTicketRegistryCleaner implements RegistryCleaner {

    /** The Commons Logging instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** The instance of the TicketRegistry to clean. */
    @NotNull
    private TicketRegistry ticketRegistry;

    /** Execution locking strategy. */
    @NotNull
    private LockingStrategy lock = new NoOpLockingStrategy();

    /** The logout manager. */
    @NotNull
    private LogoutManager logoutManager;

    /**
     * Instantiates a new default ticket registry cleaner.
     *
     * @param logoutManager the logout manager
     * @param ticketRegistry the ticket registry
     */
    public DefaultTicketRegistryCleaner(final LogoutManager logoutManager, final TicketRegistry ticketRegistry) {
        this.logoutManager = logoutManager;
        this.ticketRegistry = ticketRegistry;
    }
    
    /**
     * Instantiates a new default ticket registry cleaner.
     *
     * @param logoutManager the logout manager
     * @param ticketRegistry the ticket registry
     * @param lockingStrategy the locking strategy
     * @since 4.1
     */
    public DefaultTicketRegistryCleaner(final LogoutManager logoutManager, final TicketRegistry ticketRegistry, 
            final LockingStrategy lockingStrategy) {
        this.logoutManager = logoutManager;
        this.ticketRegistry = ticketRegistry;
    }
    
    /**
     * @see org.jasig.cas.ticket.registry.RegistryCleaner#clean()
     */
    public void clean() {
        logger.info("Beginning ticket cleanup.");
        logger.debug("Attempting to acquire ticket cleanup lock.");
        if (!this.lock.acquire()) {
            logger.info("Could not obtain lock.  Aborting cleanup.");
            return;
        }
        logger.debug("Acquired lock.  Proceeding with cleanup.");
        try {
            final List<Ticket> ticketsToRemove = new ArrayList<Ticket>();
            final Collection<Ticket> ticketsInCache;
            ticketsInCache = this.ticketRegistry.getTickets();
            for (final Ticket ticket : ticketsInCache) {
                if (ticket.isExpired()) {
                    ticketsToRemove.add(ticket);
                }
            }

            logger.info("{} expired tickets found to be removed.", ticketsToRemove.size());
            for (final Ticket ticket : ticketsToRemove) {
                if (ticket instanceof TicketGrantingTicket) {
                    this.logoutManager.performLogout((TicketGrantingTicket) ticket);
                }
                this.ticketRegistry.deleteTicket(ticket.getId());
            }
        } finally {
            logger.debug("Releasing ticket cleanup lock.");
            this.lock.release();
        }

        logger.info("Finished ticket cleanup.");
    }

    /**
     * @param ticketRegistry The ticketRegistry to set.
     * @deprecated As of 4.1. Consider using constructors instead.
     */
    @Deprecated
    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }


    /**
     * @param  strategy  Ticket cleanup locking strategy.  An exclusive locking
     * strategy is preferable if not required for some ticket backing stores,
     * such as JPA, in a clustered CAS environment.  Use {@link JpaLockingStrategy}
     * for {@link org.jasig.cas.ticket.registry.JpaTicketRegistry} in a clustered
     * CAS environment.
     * @deprecated As of 4.1. Consider using constructors instead.
     */
    @Deprecated
    public void setLock(final LockingStrategy strategy) {
        this.lock = strategy;
    }

    /**
     * @deprecated As of 4.1, single signout callbacks are entirely controlled by the {@link LogoutManager}.
     * @param logUserOutOfServices whether to logger the user out of services or not.
     */
    @Deprecated
    public void setLogUserOutOfServices(final boolean logUserOutOfServices) {
        logger.warn("Invoking setLogUserOutOfServices() is deprecated and has no impact.");
    }

    /**
     * Set the logout manager.
     *
     * @param logoutManager the logout manager.
     * @deprecated As of 4.1. Consider using constructors instead.
     */
    @Deprecated
    public void setLogoutManager(final LogoutManager logoutManager) {
        this.logoutManager = logoutManager;
    }
}
