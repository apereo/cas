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
package org.jasig.cas.support.events;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;

/**
 * Concrete subclass of {@code AbstractCasEvent} representing granting of a
 * service ticket by a CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
public final class CasServiceTicketGrantedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = 128616377249711105L;

    private final TicketGrantingTicket ticketGrantingTicket;
    private final ServiceTicket serviceTicket;

    /**
     * Instantiates a new Cas service ticket granted event.
     *
     * @param source               the source
     * @param ticketGrantingTicket the ticket granting ticket
     * @param serviceTicket        the service ticket
     */
    public CasServiceTicketGrantedEvent(final Object source, final TicketGrantingTicket ticketGrantingTicket,
                                        final ServiceTicket serviceTicket) {
        super(source);
        this.ticketGrantingTicket = ticketGrantingTicket;
        this.serviceTicket = serviceTicket;
    }

    public TicketGrantingTicket getTicketGrantingTicket() {
        return ticketGrantingTicket;
    }

    public ServiceTicket getServiceTicket() {
        return serviceTicket;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ticketGrantingTicket", ticketGrantingTicket)
                .append("serviceTicket", serviceTicket)
                .toString();
    }
}
