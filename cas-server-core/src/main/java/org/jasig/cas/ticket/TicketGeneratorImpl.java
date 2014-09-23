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
import org.jasig.cas.util.UniqueTicketIdGenerator;

import java.util.Map;

/**
 * @author Vincenzo Barrea.
 */
public class TicketGeneratorImpl extends AbstractTicketGenerator implements TicketGenerator, TicketImplementationInfo{

    /**
     * Construct a new ticket generator.
     *
     * @param ticketGrantingTicketUniqueTicketIdGenerator the TGT id generator.
     * @param uniqueTicketIdGeneratorsForService the map with service and ticket id generators.
     * @param ticketGrantingTicketExpirationPolicy the TGT expiration policy.
     * @param serviceTicketExpirationPolicy the service ticket expiration policy.
     */
    public TicketGeneratorImpl(final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator,
                               final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService,
                               final ExpirationPolicy ticketGrantingTicketExpirationPolicy,
                               final ExpirationPolicy serviceTicketExpirationPolicy) {
        super(ticketGrantingTicketUniqueTicketIdGenerator,
                uniqueTicketIdGeneratorsForService,
                ticketGrantingTicketExpirationPolicy,
                serviceTicketExpirationPolicy);
    }

    @Override
    public TicketGrantingTicket generateTicketGrantingTicket(final Authentication authentication) {
        return new TicketGrantingTicketImpl(
                this.getTicketGrantingTicketUniqueTicketIdGenerator()
                        .getNewTicketId(TicketGrantingTicket.PREFIX),
                authentication, this.getTicketGrantingTicketExpirationPolicy());
    }

    @Override
    public TicketGrantingTicket generateTicketGrantingTicket(final String id,
                                                             final TicketGrantingTicket grantingTicket,
                                                             final Authentication authentication,
                                                             final ExpirationPolicy expirationPolicy) {
        return new TicketGrantingTicketImpl(id, grantingTicket, authentication, expirationPolicy);
    }

    @Override
    public ServiceTicket generateServiceTicket(final String id,
                                               final TicketGrantingTicket ticketGrantingTicket,
                                               final Service service,
                                               final boolean fromNewLogin,
                                               final ExpirationPolicy expirationPolicy) {
        return new ServiceTicketImpl(id, ticketGrantingTicket, service, fromNewLogin, expirationPolicy);
    }

    @Override
    public Class<? extends ServiceTicket> getServiceTicketImplClass() {
        return ServiceTicketImpl.class;
    }

    @Override
    public Class<? extends TicketGrantingTicket> getTicketGrantingTicketImplClass() {
        return TicketGrantingTicketImpl.class;
    }
}
