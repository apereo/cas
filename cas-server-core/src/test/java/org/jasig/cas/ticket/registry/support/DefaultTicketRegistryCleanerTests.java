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
package org.jasig.cas.ticket.registry.support;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.CentralAuthenticationServiceImpl;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.AbstractRegistryCleanerTests;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.RegistryCleaner;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.UniqueTicketIdGenerator;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class DefaultTicketRegistryCleanerTests extends AbstractRegistryCleanerTests {

    private CentralAuthenticationService centralAuthenticationService;

    @Override
    public RegistryCleaner getNewRegistryCleaner(final TicketRegistry ticketRegistry) {
        this.centralAuthenticationService = new CentralAuthenticationServiceImpl(this.ticketRegistry,
                mock(AuthenticationManager.class), mock(UniqueTicketIdGenerator.class), Collections.EMPTY_MAP,
                new NeverExpiresExpirationPolicy(), new NeverExpiresExpirationPolicy(), mock(ServicesManager.class),
                mock(LogoutManager.class));

        return new DefaultTicketRegistryCleaner(this.centralAuthenticationService, this.ticketRegistry);
    }

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return new DefaultTicketRegistry();
    }

    @Override
    protected void afterCleaning(final Collection<Ticket> removedCol) {
        for (final Ticket ticket : removedCol) {
            this.ticketRegistry.deleteTicket(ticket.getId());
        }
    }
}
