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

import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.ticket.registry.RegistryCleaner;
import org.jasig.cas.ticket.registry.TicketRegistry;

import javax.validation.constraints.NotNull;

/**
 * Abstract implementation of a RegistryCleaner.
 *
 * @author Ahsan Rabbani
 *
 * @since 4.0
 */
public abstract class AbstractTicketRegistryCleaner implements RegistryCleaner {

    /** The instance of the TicketRegistry to clean. */
    @NotNull
    protected TicketRegistry ticketRegistry;

    /** Execution locking strategy. */
    @NotNull
    protected LockingStrategy lock = new NoOpLockingStrategy();

    /** The logout manager. */
    @NotNull
    protected LogoutManager logoutManager;

    protected TicketRegistryCleanerHelper registryCleanerHelper = new TicketRegistryCleanerHelper();

    /** If the user must be logged out of the services. */
    protected boolean logUserOutOfServices = true;

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
     * Whether to logger users out of services when we remove an expired ticket.  The default is true. Set this to
     * false to disable.
     *
     * @param logUserOutOfServices whether to logger the user out of services or not.
     */
    public void setLogUserOutOfServices(final boolean logUserOutOfServices) {
        this.logUserOutOfServices = logUserOutOfServices;
    }

    /**
     * Set the logout manager.
     *
     * @param logoutManager the logout manager.
     */
    public void setLogoutManager(final LogoutManager logoutManager) {
        this.logoutManager = logoutManager;
    }

    /**
     * Set the registry cleaner helper.
     *
     * @param registryCleanerHelper the registry cleaner helper.
     */
    void setRegistryCleanerHelper(final TicketRegistryCleanerHelper registryCleanerHelper) {
        this.registryCleanerHelper = registryCleanerHelper;
    }

}
