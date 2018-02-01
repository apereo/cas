package org.apereo.cas.ticket;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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
@Slf4j
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Setter
public abstract class AbstractTicket implements Ticket, TicketState {

    private static final long serialVersionUID = -8506442397878267555L;

    /**
     * The {@link ExpirationPolicy} this ticket is associated with.
     **/
    @Lob
    @Column(name = "EXPIRATION_POLICY", length = Integer.MAX_VALUE, nullable = false)
    private ExpirationPolicy expirationPolicy;

    /**
     * The unique identifier for this ticket.
     */
    @Id
    @Column(name = "ID", nullable = false)
    private String id;

    /**
     * The last time this ticket was used.
     */
    @Column(name = "LAST_TIME_USED")
    private ZonedDateTime lastTimeUsed;

    /**
     * The previous last time this ticket was used.
     */
    @Column(name = "PREVIOUS_LAST_TIME_USED")
    private ZonedDateTime previousTimeUsed;

    /**
     * The time the ticket was created.
     */
    @Column(name = "CREATION_TIME")
    private ZonedDateTime creationTime;

    /**
     * The number of times this was used.
     */
    @Column(name = "NUMBER_OF_TIMES_USED")
    private int countOfUses;

    /**
     * Constructs a new Ticket with a unique id, a possible parent Ticket (can
     * be null) and a specified Expiration Policy.
     *
     * @param id               the unique identifier for the ticket
     * @param expirationPolicy the expiration policy for the ticket.
     * @throws IllegalArgumentException if the id or expiration policy is null.
     */
    public AbstractTicket(@NonNull final String id, @NonNull final ExpirationPolicy expirationPolicy) {
        this.id = id;
        this.creationTime = ZonedDateTime.now(ZoneOffset.UTC);
        this.lastTimeUsed = ZonedDateTime.now(ZoneOffset.UTC);
        this.expirationPolicy = expirationPolicy;
    }

    @Override
    public void update() {
        this.previousTimeUsed = this.lastTimeUsed;
        this.lastTimeUsed = ZonedDateTime.now(ZoneOffset.UTC);
        this.countOfUses++;
        if (getTicketGrantingTicket() != null && !getTicketGrantingTicket().isExpired()) {
            final TicketState state = TicketState.class.cast(getTicketGrantingTicket());
            state.update();
        }
    }
    
    @Override
    public boolean isExpired() {
        final TicketGrantingTicket tgt = getTicketGrantingTicket();
        return this.expirationPolicy.isExpired(this) || (tgt != null && tgt.isExpired()) || isExpiredInternal();
    }

    @JsonIgnore
    protected boolean isExpiredInternal() {
        return false;
    }

    @Override
    public int compareTo(final Ticket o) {
        return getId().compareTo(o.getId());
    }
    
    @Override
    public String toString() {
        return this.getId();
    }
}
