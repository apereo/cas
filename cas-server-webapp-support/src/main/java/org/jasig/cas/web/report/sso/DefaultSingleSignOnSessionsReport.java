/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

package org.jasig.cas.web.report.sso;

import org.apache.commons.collections.Predicate;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the {@link SingleSignOnSessionsReport}.
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 4.1
 */
@Component
public class DefaultSingleSignOnSessionsReport implements SingleSignOnSessionsReport {

    @Autowired
    private CentralAuthenticationService centralAuthenticationService;

    @Override
    public Collection<Map<String, Object>> getActiveSsoSessions() {
        final List<Map<String, Object>> activeSessions = new ArrayList<Map<String, Object>>();

        for(final Ticket ticket : getNonExpiredTicketGrantingTickets()) {
            final TicketGrantingTicket tgt = (TicketGrantingTicket) ticket;

            final Map<String, Object> sso = new HashMap<>(3);
            sso.put(SsoSessionAttributeKeys.AUTHENTICATED_PRINCIPAL.toString(), tgt.getAuthentication().getPrincipal().getId());
            sso.put(SsoSessionAttributeKeys.AUTHENTICATION_DATE.toString(), tgt.getAuthentication().getAuthenticationDate());
            sso.put(SsoSessionAttributeKeys.NUMBER_OF_USES.toString(), tgt.getCountOfUses());
            activeSessions.add(Collections.unmodifiableMap(sso));
        }
        return Collections.unmodifiableCollection(activeSessions);
    }

    /**
     * Gets non expired ticket granting tickets.
     *
     * @return the non expired ticket granting tickets
     */
    private Collection<Ticket> getNonExpiredTicketGrantingTickets() {
        return this.centralAuthenticationService.getTickets(new Predicate() {
            @Override
            public boolean evaluate(final Object ticket) {
                if (ticket instanceof TicketGrantingTicket) {
                    return !((TicketGrantingTicket) ticket).isExpired();
                }
                return false;
            }
        });
    }
}
