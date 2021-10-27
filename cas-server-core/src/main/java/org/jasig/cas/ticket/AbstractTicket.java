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

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.springframework.util.Assert;

/**
 * Abstract implementation of a ticket that handles all ticket state for
 * policies. Also incorporates properties common among all tickets. As this is
 * an abstract class, it cannnot be instanciated. It is recommended that
 * implementations of the Ticket interface extend the AbstractTicket as it
 * handles common functionality amongst different ticket types (such as state
 * updating).
 *
 * AbstractTicket does not provide a protected Logger instance to
 * avoid instantiating many such Loggers at runtime (there will be many instances
 * of subclasses of AbstractTicket in a typical running CAS server).  Instead
 * subclasses should use static Logger instances.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
@MappedSuperclass
public abstract class AbstractTicket implements Ticket, TicketState {

    private static final long serialVersionUID = -8506442397878267555L;

    /** The ExpirationPolicy this ticket will be following. */
    @Lob
    @Column(name="EXPIRATION_POLICY", nullable=false)
    private ExpirationPolicy expirationPolicy;

    /** The unique identifier for this ticket. */
    @Id
    @Column(name="ID", nullable=false)
    private String id;

    /** The TicketGrantingTicket this is associated with. */
    @ManyToOne(targetEntity=TicketGrantingTicketImpl.class)
    private TicketGrantingTicket ticketGrantingTicket;

    /** The last time this ticket was used. */
    @Column(name="LAST_TIME_USED")
    private long lastTimeUsed;

    /** The previous last time this ticket was used. */
    @Column(name="PREVIOUS_LAST_TIME_USED")
    private long previousLastTimeUsed;

    /** The time the ticket was created. */
    @Column(name="CREATION_TIME")
    private long creationTime;

    /** The number of times this was used. */
    @Column(name="NUMBER_OF_TIMES_USED")
    private int countOfUses;

    protected AbstractTicket() {
        // nothing to do
    }

    /**
     * Constructs a new Ticket with a unique id, a possible parent Ticket (can
     * be null) and a specified Expiration Policy.
     *
     * @param id the unique identifier for the ticket
     * @param ticket the parent TicketGrantingTicket
     * @param expirationPolicy the expiration policy for the ticket.
     * @throws IllegalArgumentException if the id or expiration policy is null.
     */
    public AbstractTicket(final String id, final TicketGrantingTicket ticket,
        final ExpirationPolicy expirationPolicy) {
        Assert.notNull(expirationPolicy, "expirationPolicy cannot be null");
        Assert.notNull(id, "id cannot be null");

        this.id = id;
        this.creationTime = System.currentTimeMillis();
        this.lastTimeUsed = System.currentTimeMillis();
        this.expirationPolicy = expirationPolicy;
        this.ticketGrantingTicket = ticket;
    }

    public final String getId() {
        return this.id;
    }

    /**
     * Records the <i>previous</i> last time this ticket was used as well as
     * the last usage time. The ticket usage count is also incremented.
     *
     * <p>Tickets themselves are solely responsible to maintain their state. The
     * determination of  ticket usage is left up to the implementation and
     * the specific ticket type.
     *
     * @see ExpirationPolicy
     */
    protected final void updateState() {
        this.previousLastTimeUsed = this.lastTimeUsed;
        this.lastTimeUsed = System.currentTimeMillis();
        this.countOfUses++;
    }

    public final int getCountOfUses() {
        return this.countOfUses;
    }

    public final long getCreationTime() {
        return this.creationTime;
    }

    public final TicketGrantingTicket getGrantingTicket() {
        return this.ticketGrantingTicket;
    }

    public final long getLastTimeUsed() {
        return this.lastTimeUsed;
    }

    public final long getPreviousTimeUsed() {
        return this.previousLastTimeUsed;
    }

    public final boolean isExpired() {
        return this.expirationPolicy.isExpired(this)
                || (getGrantingTicket() != null && getGrantingTicket().isExpired())
                || isExpiredInternal();
    }

    protected boolean isExpiredInternal() {
        return false;
    }

    public final int hashCode() {
        return this.getId().hashCode();
    }

    public final String toString() {
        return this.getId();
    }
}
