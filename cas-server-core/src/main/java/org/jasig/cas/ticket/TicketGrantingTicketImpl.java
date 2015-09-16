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
package org.jasig.cas.ticket;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Concrete implementation of a TicketGrantingTicket. A TicketGrantingTicket is
 * the global identifier of a principal into the system. It grants the Principal
 * single-sign on access to any service that opts into single-sign on.
 * Expiration of a TicketGrantingTicket is controlled by the ExpirationPolicy
 * specified as object creation.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Entity
@Table(name="TICKETGRANTINGTICKET")
public final class TicketGrantingTicketImpl extends AbstractTicket implements TicketGrantingTicket {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -8608149809180911599L;

    /** Logger instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketGrantingTicketImpl.class);

    /** The authenticated object for which this ticket was generated for. */
    @Lob
    @Column(name="AUTHENTICATION", nullable=false, length = 1000000)
    private Authentication authentication;

    /** Flag to enforce manual expiration. */
    @Column(name="EXPIRED", nullable=false)
    private Boolean expired = Boolean.FALSE;

    /** Service that produced a proxy-granting ticket. */
    @Column(name="PROXIED_BY", nullable=true)
    private Service proxiedBy;

    /** The services associated to this ticket. */
    @Lob
    @Column(name="SERVICES_GRANTED_ACCESS_TO", nullable=false, length = 1000000)
    private final HashMap<String, Service> services = new HashMap<>();

    @Lob
    @Column(name="SUPPLEMENTAL_AUTHENTICATIONS", nullable=false, length = 1000000)
    private final ArrayList<Authentication> supplementalAuthentications = new ArrayList<>();

    /**
     * Instantiates a new ticket granting ticket impl.
     */
    public TicketGrantingTicketImpl() {
        // nothing to do
    }

    /**
     * Constructs a new TicketGrantingTicket.
     * May throw an {@link IllegalArgumentException} if the Authentication object is null.
     *
     * @param id the id of the Ticket
     * @param proxiedBy Service that produced this proxy ticket.
     * @param parentTicketGrantingTicket the parent ticket
     * @param authentication the Authentication request for this ticket
     * @param policy the expiration policy for this ticket.
     */
    public TicketGrantingTicketImpl(final String id,
        final Service proxiedBy,
        final TicketGrantingTicket parentTicketGrantingTicket,
        @NotNull final Authentication authentication, final ExpirationPolicy policy) {

        super(id, parentTicketGrantingTicket, policy);

        if (parentTicketGrantingTicket != null && proxiedBy == null) {
            throw new IllegalArgumentException("Must specify proxiedBy when providing parent TGT");
        }
        Assert.notNull(authentication, "authentication cannot be null");
        this.authentication = authentication;
        this.proxiedBy = proxiedBy;
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
        this(id, null, null, authentication, policy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Authentication getAuthentication() {
        return this.authentication;
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
        final ServiceTicket serviceTicket = new ServiceTicketImpl(id, this,
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
     * Unlike {@link Collections#unmodifiableMap(java.util.Map)},
     * which is a view of a separate map which can still change, an instance of {@link ImmutableMap}
     * contains its own data and will never change.
     *
     * @return an immutable map of service ticket and services accessed by this ticket-granting ticket.
    */
    @Override
    public synchronized Map<String, Service> getServices() {
        return ImmutableMap.copyOf(this.services);
    }

    /**
     * Remove all services of the TGT (at logout).
     */
    @Override
    public void removeAllServices() {
        services.clear();
    }

    /**
     * Return if the TGT has no parent.
     *
     * @return if the TGT has no parent.
     */
    @Override
    public boolean isRoot() {
        return this.getGrantingTicket() == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markTicketExpired() {
        this.expired = Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TicketGrantingTicket getRoot() {
        TicketGrantingTicket current = this;
        TicketGrantingTicket parent = current.getGrantingTicket();
        while (parent != null) {
            current = parent;
            parent = current.getGrantingTicket();
        }
        return current;
    }

    /**
     * Return if the TGT is expired.
     *
     * @return if the TGT is expired.
     */
    @Override
    public boolean isExpiredInternal() {
        return this.expired;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Authentication> getSupplementalAuthentications() {
        return this.supplementalAuthentications;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Authentication> getChainedAuthentications() {
        final List<Authentication> list = new ArrayList<>();

        list.add(getAuthentication());

        if (getGrantingTicket() == null) {
            return Collections.unmodifiableList(list);
        }

        list.addAll(getGrantingTicket().getChainedAuthentications());
        return Collections.unmodifiableList(list);
    }

    @Override
    public Service getProxiedBy() {
        return this.proxiedBy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (!(object instanceof TicketGrantingTicket)) {
            return false;
        }

        final Ticket ticket = (Ticket) object;

        return new EqualsBuilder()
                .append(ticket.getId(), this.getId())
                .isEquals();
    }

}
