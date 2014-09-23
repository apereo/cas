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
import org.jasig.cas.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * @author Vincenzo Barrea.
 */
@MappedSuperclass
public abstract class AbstractServiceTicket extends AbstractTicket implements ServiceTicket{

    @Column(name="TICKET_ALREADY_GRANTED", nullable=false)
    private Boolean grantedTicketAlready = false;

    /**
     * Instantiates a new abstract service ticket.
     */
    protected AbstractServiceTicket() {
        // nothing to do
    }

    /**
     * Constructs a new Ticket with a unique id, a possible parent Ticket (can
     * be null) and a specified Expiration Policy.
     *
     * @param id the unique identifier for the ticket
     * @param expirationPolicy the expiration policy for the ticket.
     * @throws IllegalArgumentException if the id or expiration policy is null.
     */
    protected AbstractServiceTicket(final String id, final ExpirationPolicy expirationPolicy) {
        super(id, expirationPolicy);
    }

    @Override
    public TicketGrantingTicket grantTicketGrantingTicket(
            final String id, final Authentication authentication,
            final ExpirationPolicy expirationPolicy) {
        synchronized (this) {
            if(this.grantedTicketAlready) {
                throw new IllegalStateException(
                        "TicketGrantingTicket already generated for this ServiceTicket.  Cannot grant more than one TGT for ServiceTicket");
            }
            this.grantedTicketAlready = true;
        }

        final ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        if (applicationContext==null){
            throw new IllegalStateException(
                    "Cannot find application context. Check your configuration");
        }

        final TicketGenerator ticketGenerator = applicationContext.getBean(TicketGenerator.class);

        return ticketGenerator.generateTicketGrantingTicket(id, this.getGrantingTicket(), authentication, expirationPolicy);

    }
}
