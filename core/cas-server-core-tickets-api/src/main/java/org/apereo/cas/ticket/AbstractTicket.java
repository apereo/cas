package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Abstract implementation of a ticket that handles all ticket state for
 * policies. Also incorporates properties common among all tickets. As this is
 * an abstract class, it cannot be created. It is recommended that
 * implementations of the Ticket interface extend the AbstractTicket as it
 * handles common functionality amongst different ticket types (such as state
 * updating).
 * <p>
 * AbstractTicket does not provide a logger instance to
 * avoid instantiating many such Loggers at runtime (there will be many instances
 * of subclasses of AbstractTicket in a typical running CAS server).  Instead
 * subclasses should use static Logger instances.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@MappedSuperclass
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Setter
@Slf4j
public abstract class AbstractTicket implements Ticket, TicketState {

    private static final long serialVersionUID = -8506442397878267555L;

    /**
     * The {@link ExpirationPolicy} this ticket is associated with.
     **/
    @Lob
    @Column(name = "EXPIRATION_POLICY", length = Integer.MAX_VALUE, nullable = false)
    @Getter
    private ExpirationPolicy expirationPolicy;

    /**
     * The unique identifier for this ticket.
     */
    @Id
    @Column(name = "ID", nullable = false)
    @Getter
    private String id;

    /**
     * The last time this ticket was used.
     */
    @Column(name = "LAST_TIME_USED", length = Integer.MAX_VALUE)
    @Getter
    private ZonedDateTime lastTimeUsed;

    /**
     * The previous last time this ticket was used.
     */
    @Column(name = "PREVIOUS_LAST_TIME_USED", length = Integer.MAX_VALUE)
    @Getter
    private ZonedDateTime previousTimeUsed;

    /**
     * The time the ticket was created.
     */
    @Column(name = "CREATION_TIME", length = Integer.MAX_VALUE)
    @Getter
    private ZonedDateTime creationTime;

    /**
     * The number of times this was used.
     */
    @Column(name = "NUMBER_OF_TIMES_USED")
    @Getter
    private int countOfUses;

    /**
     * Flag to enforce manual expiration.
     */
    @Column(name = "EXPIRED", nullable = false)
    private Boolean expired = Boolean.FALSE;


    public AbstractTicket(final String id, final ExpirationPolicy expirationPolicy) {
        this.id = id;
        this.creationTime = ZonedDateTime.now(expirationPolicy.getClock());
        this.lastTimeUsed = this.creationTime;
        this.expirationPolicy = expirationPolicy;
    }

    @Override
    public void update() {
        updateTicketState();
        updateTicketGrantingTicketState();
    }

    /**
     * Update ticket granting ticket state.
     */
    protected void updateTicketGrantingTicketState() {
        val ticketGrantingTicket = getTicketGrantingTicket();
        if (ticketGrantingTicket != null && !ticketGrantingTicket.isExpired()) {
            val state = TicketState.class.cast(ticketGrantingTicket);
            state.update();
        }
    }

    /**
     * Update ticket state.
     */
    protected void updateTicketState() {
        LOGGER.trace("Before updating ticket [{}]\n\tPrevious time used: [{}]\n\tLast time used: [{}]\n\tUsage count: [{}]",
            getId(), this.previousTimeUsed, this.lastTimeUsed, this.countOfUses);

        this.previousTimeUsed = ZonedDateTime.from(this.lastTimeUsed);
        this.lastTimeUsed = ZonedDateTime.now(this.expirationPolicy.getClock());
        this.countOfUses++;

        LOGGER.trace("After updating ticket [{}]\n\tPrevious time used: [{}]\n\tLast time used: [{}]\n\tUsage count: [{}]",
            getId(), this.previousTimeUsed, this.lastTimeUsed, this.countOfUses);
    }

    @Override
    public boolean isExpired() {
        return this.expirationPolicy.isExpired(this) || isExpiredInternal();
    }

    @JsonIgnore
    protected boolean isExpiredInternal() {
        return this.expired;
    }

    @Override
    public int compareTo(final Ticket o) {
        return getId().compareTo(o.getId());
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public Authentication getAuthentication() {
        val ticketGrantingTicket = getTicketGrantingTicket();
        return Optional.ofNullable(ticketGrantingTicket)
            .map(TicketGrantingTicket::getAuthentication)
            .orElse(null);
    }

    @Override
    public TicketGrantingTicket getTicketGrantingTicket() {
        return null;
    }

    @Override
    public void markTicketExpired() {
        this.expired = Boolean.TRUE;
    }
}
