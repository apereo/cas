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

package org.jasig.cas.util;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;

import java.util.Map;

/**
 * Default implementation of {@link org.jasig.cas.util.AuthenticationSupport}.
 * <p/>
 * Uses CAS' {@link org.jasig.cas.ticket.registry.TicketRegistry}
 * to retrieve TGT and its associated objects by provided tgt String token
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 */
public final class DefaultAuthenticationSupport implements AuthenticationSupport {

    private final TicketRegistry ticketRegistry;

    /**
     * Instantiates a new Default authentication support.
     *
     * @param ticketRegistry the ticket registry
     */
    public DefaultAuthenticationSupport(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    @Override
    /** {@inheritDoc} */
    public Authentication getAuthenticationFrom(final String ticketGrantingTicketId) {
        final TicketGrantingTicket tgt = (TicketGrantingTicket)
                this.ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        return tgt == null ? null : tgt.getAuthentication();
    }

    @Override
    /** {@inheritDoc} */
    public Principal getAuthenticatedPrincipalFrom(final String ticketGrantingTicketId) {
        final Authentication auth = getAuthenticationFrom(ticketGrantingTicketId);
        return auth == null ? null : auth.getPrincipal();
    }

    @Override
    /** {@inheritDoc} */
    public Map<String, Object> getPrincipalAttributesFrom(final String ticketGrantingTicketId) {
        final Principal principal = getAuthenticatedPrincipalFrom(ticketGrantingTicketId);
        return principal == null ? null : principal.getAttributes();
    }
}
