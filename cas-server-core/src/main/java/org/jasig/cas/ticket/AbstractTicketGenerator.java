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
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.UnauthorizedSsoServiceException;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author Vincenzo Barrea.
 */
public abstract class AbstractTicketGenerator implements TicketGenerator {

    /** Log instance for logging events, info, warnings, errors, etc. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * UniqueTicketIdGenerator to generate ids for TicketGrantingTickets
     * created.
     */
    @NotNull
    private final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    /** Map to contain the mappings of service->UniqueTicketIdGenerators. */
    @NotNull
    private final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService;

    /** Expiration policy for ticket granting tickets. */
    @NotNull
    private ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    /** ExpirationPolicy for Service Tickets. */
    @NotNull
    private ExpirationPolicy serviceTicketExpirationPolicy;

    /**
     * Construct a new ticket generator.
     *
     * @param ticketGrantingTicketUniqueTicketIdGenerator the TGT id generator.
     * @param uniqueTicketIdGeneratorsForService the map with service and ticket id generators.
     * @param ticketGrantingTicketExpirationPolicy the TGT expiration policy.
     * @param serviceTicketExpirationPolicy the service ticket expiration policy.
     */
    public AbstractTicketGenerator(final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator,
                               final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService,
                               final ExpirationPolicy ticketGrantingTicketExpirationPolicy,
                               final ExpirationPolicy serviceTicketExpirationPolicy) {
        this.ticketGrantingTicketUniqueTicketIdGenerator = ticketGrantingTicketUniqueTicketIdGenerator;
        this.uniqueTicketIdGeneratorsForService = uniqueTicketIdGeneratorsForService;
        this.ticketGrantingTicketExpirationPolicy = ticketGrantingTicketExpirationPolicy;
        this.serviceTicketExpirationPolicy = serviceTicketExpirationPolicy;
    }

    @Override
    public ServiceTicket generateServiceTicket(final TicketGrantingTicket ticketGrantingTicket,
                                               final Service service, final boolean credentialsProvided){
        final UniqueTicketIdGenerator serviceTicketUniqueTicketIdGenerator = getServiceTicketUniqueTicketIdGenerator(service);
        final List<Authentication> authentications = ticketGrantingTicket.getChainedAuthentications();
        final String ticketPrefix = authentications.size() == 1 ? ServiceTicket.PREFIX : ServiceTicket.PROXY_TICKET_PREFIX;
        final String ticketId = serviceTicketUniqueTicketIdGenerator.getNewTicketId(ticketPrefix);
        final ServiceTicket serviceTicket = ticketGrantingTicket.grantServiceTicket(ticketId, service,
                this.serviceTicketExpirationPolicy, credentialsProvided);
        return serviceTicket;
    }

    @Override
    public TicketGrantingTicket generateProxyGrantingTicket(final ServiceTicket serviceTicket, final Authentication authentication) {
        final String pgtId = this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(
                TicketGrantingTicket.PROXY_GRANTING_TICKET_PREFIX);
        return serviceTicket.grantTicketGrantingTicket(pgtId,
                authentication, this.ticketGrantingTicketExpirationPolicy);
    }

    /**
     * Returns an UniqueTicketIdGenerator for the specified service.
     *
     * @param service the service for which we are finding an id generator
     * @return the UniqueTicketIdGenerator for the specified service
     */
    private UniqueTicketIdGenerator getServiceTicketUniqueTicketIdGenerator(final Service service) {
        final String uniqueTicketIdGenKey = service.getClass().getName();
        if (!this.uniqueTicketIdGeneratorsForService.containsKey(uniqueTicketIdGenKey)) {
            logger.warn("Cannot create service ticket because the key [{}] for service [{}] is not linked to a ticket id generator",
                    uniqueTicketIdGenKey, service.getId());
            throw new UnauthorizedSsoServiceException();
        }

        return this.uniqueTicketIdGeneratorsForService.get(uniqueTicketIdGenKey);
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

    protected UniqueTicketIdGenerator getTicketGrantingTicketUniqueTicketIdGenerator() {
        return ticketGrantingTicketUniqueTicketIdGenerator;
    }

    protected Map<String, UniqueTicketIdGenerator> getUniqueTicketIdGeneratorsForService() {
        return uniqueTicketIdGeneratorsForService;
    }

    protected ExpirationPolicy getTicketGrantingTicketExpirationPolicy() {
        return ticketGrantingTicketExpirationPolicy;
    }

    protected ExpirationPolicy getServiceTicketExpirationPolicy() {
        return serviceTicketExpirationPolicy;
    }
}
