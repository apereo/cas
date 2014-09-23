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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jasig.cas.authentication.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Column;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Concrete implementation of a TicketGrantingTicket. A TicketGrantingTicket is
 * the global identifier of a principal into the system. It grants the Principal
 * single-sign on access to any service that opts into single-sign on.
 * Expiration of a TicketGrantingTicket is controlled by the ExpirationPolicy
 * specified as object creation.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
@Entity
@Table(name="TICKETGRANTINGTICKET")
public final class TicketGrantingTicketImpl extends AbstractTicketGrantingTicket implements TicketGrantingTicket {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -8608149809180911599L;

    /** Logger instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketGrantingTicketImpl.class);

    /** The TicketGrantingTicket this is associated with. */
    @ManyToOne(targetEntity = TicketGrantingTicketImpl.class)
    private TicketGrantingTicket ticketGrantingTicket;

    /** The authenticated object for which this ticket was generated for. */
    @Lob
    @Column(name="AUTHENTICATION", nullable=false)
    private Authentication authentication;

    /** Flag to enforce manual expiration. */
    @Column(name="EXPIRED", nullable=false)
    private Boolean expired = false;

    @Lob
    @Column(name="SUPPLEMENTAL_AUTHENTICATIONS", nullable=false)
    private final ArrayList<Authentication> supplementalAuthentications = new ArrayList<Authentication>();

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
     * @param ticketGrantingTicket the parent TicketGrantingTicket
     * @param authentication the Authentication request for this ticket
     * @param policy the expiration policy for this ticket.
     */
    public TicketGrantingTicketImpl(final String id,
        final TicketGrantingTicket ticketGrantingTicket,
        final Authentication authentication, final ExpirationPolicy policy) {
        super(id, policy);

        Assert.notNull(authentication, "authentication cannot be null");

        this.ticketGrantingTicket = ticketGrantingTicket;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Authentication getAuthentication() {
        return this.authentication;
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

    /** {@inheritDoc} */
    @Override
    public void markTicketExpired() {
        this.expired = true;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public List<Authentication> getSupplementalAuthentications() {
        return this.supplementalAuthentications;
    }

    /** {@inheritDoc} */
    @Override
    public List<Authentication> getChainedAuthentications() {
        final List<Authentication> list = new ArrayList<Authentication>();

        list.add(getAuthentication());

        if (getGrantingTicket() == null) {
            return Collections.unmodifiableList(list);
        }

        list.addAll(getGrantingTicket().getChainedAuthentications());
        return Collections.unmodifiableList(list);
    }

    public TicketGrantingTicket getGrantingTicket() {
        return this.ticketGrantingTicket;
    }

    /** {@inheritDoc} */
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
