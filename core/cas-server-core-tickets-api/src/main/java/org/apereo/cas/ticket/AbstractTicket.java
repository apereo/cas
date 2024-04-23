package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
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
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@Setter
@Slf4j
public abstract class AbstractTicket implements TicketGrantingTicketAwareTicket, PropertiesAwareTicket {

    @Serial
    private static final long serialVersionUID = -8506442397878267555L;

    /**
     * The {@link ExpirationPolicy} this ticket is associated with.
     **/
    @Getter
    private ExpirationPolicy expirationPolicy;

    /**
     * The unique identifier for this ticket.
     */
    @Getter
    private String id;

    /**
     * The last time this ticket was used.
     */
    @Getter
    private ZonedDateTime lastTimeUsed;

    /**
     * The previous last time this ticket was used.
     */
    @Getter
    private ZonedDateTime previousTimeUsed;

    /**
     * The time the ticket was created.
     */
    @Getter
    private ZonedDateTime creationTime;

    /**
     * The number of times this was used.
     */
    @Getter
    private int countOfUses;

    /**
     * Flag to enforce manual expiration.
     */
    private Boolean expired = Boolean.FALSE;
    
    private Boolean stateless = Boolean.FALSE;

    @Getter
    private Map<String, Object> properties = new HashMap<>(0);
    
    protected AbstractTicket(final String id, final ExpirationPolicy expirationPolicy) {
        this.id = id;
        this.creationTime = ZonedDateTime.now(expirationPolicy.getClock());
        this.lastTimeUsed = this.creationTime;
        this.expirationPolicy = expirationPolicy;
    }

    @Override
    public boolean isExpired() {
        return this.expirationPolicy.isExpired(this) || isExpiredInternal();
    }

    @Override
    public boolean isStateless() {
        return this.stateless;
    }

    @Override
    public void markTicketExpired() {
        this.expired = Boolean.TRUE;
    }

    @Override
    @CanIgnoreReturnValue
    public Ticket markTicketStateless() {
        this.stateless = Boolean.TRUE;
        return this;
    }

    @Override
    public void update() {
        updateTicketState();
        updateTicketGrantingTicketState();
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
            .map(AuthenticationAwareTicket.class::cast)
            .map(AuthenticationAwareTicket::getAuthentication)
            .orElse(null);
    }

    @Override
    public Ticket getTicketGrantingTicket() {
        return null;
    }

    /**
     * Update ticket granting ticket state.
     */
    protected void updateTicketGrantingTicketState() {
        val ticketGrantingTicket = getTicketGrantingTicket();
        if (ticketGrantingTicket != null && !ticketGrantingTicket.isExpired()) {
            ticketGrantingTicket.update();
        }
    }

    /**
     * Update ticket state.
     */
    @SuppressWarnings("FromTemporalAccessor")
    protected void updateTicketState() {
        LOGGER.trace("Before updating ticket [{}]\n\tPrevious time used: [{}]\n\tLast time used: [{}]\n\tUsage count: [{}]",
            getId(), this.previousTimeUsed, this.lastTimeUsed, this.countOfUses);

        this.previousTimeUsed = ZonedDateTime.from(this.lastTimeUsed);
        this.lastTimeUsed = ZonedDateTime.now(this.expirationPolicy.getClock());
        this.countOfUses++;

        LOGGER.trace("After updating ticket [{}]\n\tPrevious time used: [{}]\n\tLast time used: [{}]\n\tUsage count: [{}]",
            getId(), this.previousTimeUsed, this.lastTimeUsed, this.countOfUses);
    }

    @JsonIgnore
    protected boolean isExpiredInternal() {
        return this.expired;
    }

    @Override
    public void putProperty(final String name, final Serializable value) {
        this.properties.put(name, value);
    }

    @Override
    public void putAllProperties(final Map<String, Serializable> props) {
        this.properties.putAll(props);
    }

    @Override
    public boolean containsProperty(final String name) {
        return this.properties.containsKey(name);
    }

    @Override
    public <T> T getProperty(final String name, final Class<T> clazz) {
        if (containsProperty(name)) {
            return clazz.cast(this.properties.get(name));
        }
        return null;
    }

    @Override
    public <T extends Serializable> T getProperty(final String name, final Class<T> clazz, final T defaultValue) {
        if (containsProperty(name)) {
            return clazz.cast(this.properties.getOrDefault(name, defaultValue));
        }
        return defaultValue;
    }
}
