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
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Ahsan Rabbani
 *
 * @since 4.0
 */
public class TicketRegistryCleanerHelper {

    /** The Commons Logging instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public int deleteExpiredTickets(
            final TicketRegistry ticketRegistry,
            final LogoutManager logoutManager,
            final Collection<Ticket> ticketsInCache,
            final boolean logUserOutOfServices) {
        int numTicketsDeleted = 0;

        final List<Ticket> ticketsToRemove = new ArrayList<Ticket>();
        for (final Ticket ticket : ticketsInCache) {
            if (ticket.isExpired()) {
                ticketsToRemove.add(ticket);
            }
        }

        logger.info("{} tickets found to be removed.", ticketsToRemove.size());
        for (final Ticket ticket : ticketsToRemove) {
            // CAS-686: Expire TGT to trigger single sign-out
            if (logUserOutOfServices && ticket instanceof TicketGrantingTicket) {
                logoutManager.performLogout((TicketGrantingTicket) ticket);
            }
            ticketRegistry.deleteTicket(ticket.getId());
            numTicketsDeleted++;
        }

        return numTicketsDeleted;
    }

}
