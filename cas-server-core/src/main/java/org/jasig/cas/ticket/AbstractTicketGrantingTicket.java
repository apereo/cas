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
import org.jasig.cas.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vincenzo Barrea.
 */
@MappedSuperclass
public abstract class AbstractTicketGrantingTicket extends AbstractTicket implements TicketGrantingTicket {

    /** The services associated to this ticket. */
    @Lob
    @Column(name="SERVICES_GRANTED_ACCESS_TO", nullable=false)
    private final HashMap<String, Service> services = new HashMap<String, Service>();

    /**
     * Instantiates a new abstract ticket granting ticket.
     */
    public AbstractTicketGrantingTicket() {
    }

    /**
     * Constructs a new Ticket with a unique id, a possible parent Ticket (can
     * be null) and a specified Expiration Policy.
     *
     * @param id the unique identifier for the ticket
     * @param expirationPolicy the expiration policy for the ticket.
     * @throws IllegalArgumentException if the id or expiration policy is null.
     */
    public AbstractTicketGrantingTicket(final String id, final ExpirationPolicy expirationPolicy) {
        super(id, expirationPolicy);
    }

    /**
     * {@inheritDoc}
     * <p>The state of the ticket is affected by this operation and the
     * ticket will be considered used. The state update subsequently may
     * impact the ticket expiration policy in that, depending on the policy
     * configuration, the ticket may be considered expired.
     */
    @Override
    public synchronized ServiceTicket grantServiceTicket(final String id,
                                                         final Service service, final ExpirationPolicy expirationPolicy,
                                                         final boolean credentialsProvided) {

        final ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        if (applicationContext==null){
            throw new IllegalStateException(
                    "Cannot find application context. Check your configuration");
        }

        final TicketGenerator ticketGenerator = applicationContext.getBean(TicketGenerator.class);

        final ServiceTicket serviceTicket = ticketGenerator.generateServiceTicket(id, this,
                service, this.getCountOfUses() == 0 || credentialsProvided,
                expirationPolicy);

        updateState();

        final List<Authentication> authentications = getChainedAuthentications();
        service.setPrincipal(authentications.get(authentications.size()-1).getPrincipal());

        this.services.put(id, service);

        return serviceTicket;
    }

    /**
     * Gets an immutable map of service ticket and services accessed by this ticket-granting ticket.
     *
     * @return an immutable map of service ticket and services accessed by this ticket-granting ticket.
     */
    @Override
    public synchronized Map<String, Service> getServices() {
        final Map<String, Service> map = new HashMap<String, Service>(services.size());
        for (final String ticket : services.keySet()) {
            map.put(ticket, services.get(ticket));
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Remove all services of the TGT (at logout).
     */
    @Override
    public void removeAllServices() {
        services.clear();
    }
}
