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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Concrete implementation of a TicketGrantingTicket. A TicketGrantingTicket is
 * the global identifier of a principal into the system. It grants the Principal
 * single-sign on access to any service that opts into single-sign on.
 * Expiration of a TicketGrantingTicket is controlled by the ExpirationPolicy
 * specified as object creation.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.3 $ $Date: 2007/02/20 14:41:04 $
 * @since 3.0
 */
@Entity
@Table(name="TICKETGRANTINGTICKET")
public final class TicketGrantingTicketImpl extends AbstractTicket implements
    TicketGrantingTicket {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -5197946718924166491L;

    private static final Logger LOG = LoggerFactory.getLogger(TicketGrantingTicketImpl.class);

    /** The authenticated object for which this ticket was generated for. */
    @Lob
    @Column(name="AUTHENTICATION", nullable=false)
    private Authentication authentication;

    /** Flag to enforce manual expiration. */
    @Column(name="EXPIRED", nullable=false)
    private Boolean expired = false;
    
    @Lob
    @Column(name="SERVICES_GRANTED_ACCESS_TO", nullable=false)
    private final HashMap<String,Service> services = new HashMap<String, Service>();
    
    public TicketGrantingTicketImpl() {
        // nothing to do
    }

    /**
     * Constructs a new TicketGrantingTicket.
     * 
     * @param id the id of the Ticket
     * @param ticketGrantingTicket the parent ticket
     * @param authentication the Authentication request for this ticket
     * @param policy the expiration policy for this ticket.
     * @throws IllegalArgumentException if the Authentication object is null
     */
    public TicketGrantingTicketImpl(final String id,
        final TicketGrantingTicketImpl ticketGrantingTicket,
        final Authentication authentication, final ExpirationPolicy policy) {
        super(id, ticketGrantingTicket, policy);

        Assert.notNull(authentication, "authentication cannot be null");

        this.authentication = authentication;
    }

    /**
     * Constructs a new TicketGrantingTicket without a parent
     * TicketGrantingTicket.
     * 
     * @param id the id of the Ticket
     * @param authentication the Authentication request for this ticket
     * @param policy the expiration policy for this ticket.
     */
    public TicketGrantingTicketImpl(final String id,
        final Authentication authentication, final ExpirationPolicy policy) {
        this(id, null, authentication, policy);
    }

    public Authentication getAuthentication() {
        return this.authentication;
    }

    public synchronized ServiceTicket grantServiceTicket(final String id,
        final Service service, final ExpirationPolicy expirationPolicy,
        final boolean credentialsProvided) {
        final ServiceTicket serviceTicket = new ServiceTicketImpl(id, this,
            service, this.getCountOfUses() == 0 || credentialsProvided,
            expirationPolicy);

        updateState();
        
        final List<Authentication> authentications = getChainedAuthentications();
        service.setPrincipal(authentications.get(authentications.size()-1).getPrincipal());
        
        this.services.put(id, service);

        return serviceTicket;
    }
    
    private void logOutOfServices() {
        for (final Entry<String, Service> entry : this.services.entrySet()) {

            if (!entry.getValue().logOutOfService(entry.getKey())) {
                LOG.warn("Logout message not sent to [" + entry.getValue().getId() + "]; Continuing processing...");   
            }
        }
    }

    public boolean isRoot() {
        return this.getGrantingTicket() == null;
    }

    public synchronized void expire() {
        this.expired = true;
        logOutOfServices();
    }

    public boolean isExpiredInternal() {
        return this.expired;
    }

    public List<Authentication> getChainedAuthentications() {
        final List<Authentication> list = new ArrayList<Authentication>();

        if (this.getGrantingTicket() == null) {
            list.add(this.getAuthentication());
            return Collections.unmodifiableList(list);
        }

        list.add(this.getAuthentication());
        list.addAll(this.getGrantingTicket().getChainedAuthentications());

        return Collections.unmodifiableList(list);
    }
    
    public final boolean equals(final Object object) {
        if (object == null
            || !(object instanceof TicketGrantingTicket)) {
            return false;
        }

        final Ticket ticket = (Ticket) object;
        
        return ticket.getId().equals(this.getId());
    }
}
