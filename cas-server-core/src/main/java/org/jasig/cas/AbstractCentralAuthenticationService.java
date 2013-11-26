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
package org.jasig.cas;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.AcceptAnyAuthenticationPolicyFactory;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.jasig.cas.authentication.principal.PersistentIdGenerator;
import org.jasig.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.services.RegisteredServiceAttributeFilter;
import org.jasig.cas.services.ServiceContext;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.support.RegisteredServiceDefaultAttributeFilter;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract implementation of the {@link CentralAuthenticationService} that provides access to
 * the needed scaffolding and services that are necessary to CAS, such as ticket registry, service registry, etc.
 * The intension here is to allow extensions to easily benefit these already-configured components
 * without having to to duplicate them again.
 * @author Misagh Moayyed
 * @since 4.0
 * @see CentralAuthenticationServiceImpl
 */
public abstract class AbstractCentralAuthenticationService implements CentralAuthenticationService {

    /** Log instance for logging events, info, warnings, errors, etc. */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /** TicketRegistry for storing and retrieving tickets as needed. */
    @NotNull
    protected final TicketRegistry ticketRegistry;

    /** New Ticket Registry for storing and retrieving services tickets. Can point to the same one as the ticketRegistry variable. */
    @NotNull
    protected final TicketRegistry serviceTicketRegistry;

    /**
     * AuthenticationManager for authenticating credentials for purposes of
     * obtaining tickets.
     */
    @NotNull
    protected final AuthenticationManager authenticationManager;

    /**
     * UniqueTicketIdGenerator to generate ids for TicketGrantingTickets created.
     */
    @NotNull
    protected final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    /** Map to contain the mappings of service->UniqueTicketIdGenerators. */
    @NotNull
    protected final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService;

    /** Implementation of Service Manager. */
    @NotNull
    protected final ServicesManager servicesManager;

    /** The logout manager. **/
    @NotNull
    protected final LogoutManager logoutManager;

    /** Expiration policy for ticket granting tickets. */
    @NotNull
    protected ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    /** ExpirationPolicy for Service Tickets. */
    @NotNull
    protected ExpirationPolicy serviceTicketExpirationPolicy;

    /** Encoder to generate PseudoIds. */
    @NotNull
    protected PersistentIdGenerator persistentIdGenerator = new ShibbolethCompatiblePersistentIdGenerator();

    /** The default attribute filter to match principal attributes against that of a registered service. **/
    protected RegisteredServiceAttributeFilter defaultAttributeFilter = new RegisteredServiceDefaultAttributeFilter();

    /**
     * Authentication policy that uses a service context to produce stateful security policies to apply when
     * authenticating credentials.
     */
    @NotNull
    protected ContextualAuthenticationPolicyFactory<ServiceContext> serviceContextAuthenticationPolicyFactory =
            new AcceptAnyAuthenticationPolicyFactory();

    /**
     * Build the central authentication service implementation.
     *
     * @param ticketRegistry the tickets registry.
     * @param serviceTicketRegistry the service tickets registry.
     * @param authenticationManager the authentication manager.
     * @param ticketGrantingTicketUniqueTicketIdGenerator the TGT id generator.
     * @param uniqueTicketIdGeneratorsForService the map with service and ticket id generators.
     * @param ticketGrantingTicketExpirationPolicy the TGT expiration policy.
     * @param serviceTicketExpirationPolicy the service ticket expiration policy.
     * @param servicesManager the services manager.
     * @param logoutManager the logout manager.
     */
    public AbstractCentralAuthenticationService(final TicketRegistry ticketRegistry,
                                            final TicketRegistry serviceTicketRegistry,
                                            final AuthenticationManager authenticationManager,
                                            final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator,
                                            final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService,
                                            final ExpirationPolicy ticketGrantingTicketExpirationPolicy,
                                            final ExpirationPolicy serviceTicketExpirationPolicy,
                                            final ServicesManager servicesManager,
                                            final LogoutManager logoutManager) {
        this.ticketRegistry = ticketRegistry;
        if (serviceTicketRegistry == null) {
            this.serviceTicketRegistry = ticketRegistry;
        } else {
            this.serviceTicketRegistry = serviceTicketRegistry;
        }
        this.authenticationManager = authenticationManager;
        this.ticketGrantingTicketUniqueTicketIdGenerator = ticketGrantingTicketUniqueTicketIdGenerator;
        this.uniqueTicketIdGeneratorsForService = uniqueTicketIdGeneratorsForService;
        this.ticketGrantingTicketExpirationPolicy = ticketGrantingTicketExpirationPolicy;
        this.serviceTicketExpirationPolicy = serviceTicketExpirationPolicy;
        this.servicesManager = servicesManager;
        this.logoutManager = logoutManager;
    }
    
    public void setPersistentIdGenerator(final PersistentIdGenerator persistentIdGenerator) {
        this.persistentIdGenerator = persistentIdGenerator;
    }

    public void setServiceContextAuthenticationPolicyFactory(final ContextualAuthenticationPolicyFactory<ServiceContext> policy) {
        this.serviceContextAuthenticationPolicyFactory = policy;
    }

    /**
     * @param ticketGrantingTicketExpirationPolicy a TGT expiration policy.
     */
    public void setTicketGrantingTicketExpirationPolicy(final ExpirationPolicy ticketGrantingTicketExpirationPolicy) {
        this.ticketGrantingTicketExpirationPolicy = ticketGrantingTicketExpirationPolicy;
    }

    /**
     * @param serviceTicketExpirationPolicy a ST expiration policy.
     */
    public void setServiceTicketExpirationPolicy(final ExpirationPolicy serviceTicketExpirationPolicy) {
        this.serviceTicketExpirationPolicy = serviceTicketExpirationPolicy;
    }
}
