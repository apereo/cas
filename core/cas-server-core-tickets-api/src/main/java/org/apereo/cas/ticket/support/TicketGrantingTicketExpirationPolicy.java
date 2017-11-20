package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Provides the Ticket Granting Ticket expiration policy.  Ticket Granting Tickets
 * can be used any number of times, have a fixed lifetime, and an idle timeout.
 *
 * @author William G. Thompson, Jr.
 * @since 3.4.10
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class TicketGrantingTicketExpirationPolicy extends AbstractCasExpirationPolicy {

    /**
     * Serialization support.
     */
    private static final long serialVersionUID = 7670537200691354820L;

    /**
     * The Logger instance for this class. Using a transient instance field for the Logger doesn't work, on object
     * deserialization the field is null.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketGrantingTicketExpirationPolicy.class);

    /**
     * Maximum time this ticket is valid.
     */
    private long maxTimeToLiveInSeconds;

    /**
     * Time to kill in seconds.
     */
    private long timeToKillInSeconds;

    public TicketGrantingTicketExpirationPolicy() {
    }

    /**
     * Instantiates a new Ticket granting ticket expiration policy.
     *
     * @param maxTimeToLive the max time to live
     * @param timeToKill    the time to kill
     */
    @JsonCreator
    public TicketGrantingTicketExpirationPolicy(@JsonProperty("timeToLive") final long maxTimeToLive,
                                                @JsonProperty("timeToIdle") final long timeToKill) {
        this.maxTimeToLiveInSeconds = maxTimeToLive;
        this.timeToKillInSeconds = timeToKill;
    }

    /**
     * After properties set.
     */
    @PostConstruct
    public void afterPropertiesSet() {
        Assert.isTrue(this.maxTimeToLiveInSeconds >= this.timeToKillInSeconds,
                "maxTimeToLiveInSeconds must be greater than or equal to timeToKillInSeconds.");
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        final ZonedDateTime currentSystemTime = getCurrentSystemTime();
        final ZonedDateTime creationTime = ticketState.getCreationTime();
        final ZonedDateTime lastTimeUsed = ticketState.getLastTimeUsed();

        // Ticket has been used, check maxTimeToLive (hard window)
        ZonedDateTime expirationTime = creationTime.plus(this.maxTimeToLiveInSeconds, ChronoUnit.SECONDS);
        if (currentSystemTime.isAfter(expirationTime)) {
            LOGGER.debug("Ticket is expired because the time since creation [{}] is greater than current system time", expirationTime, currentSystemTime);
            return true;
        }

        expirationTime = lastTimeUsed.plus(this.timeToKillInSeconds, ChronoUnit.SECONDS);
        if (currentSystemTime.isAfter(expirationTime)) {
            LOGGER.debug("Ticket is expired because the time since last use is greater than timeToKillInSeconds");
            return true;
        }

        return false;
    }

    /**
     * Gets current system time.
     *
     * @return the current system time
     */
    @JsonIgnore
    protected ZonedDateTime getCurrentSystemTime() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public Long getTimeToLive() {
        return this.maxTimeToLiveInSeconds;
    }

    @Override
    public Long getTimeToIdle() {
        return this.timeToKillInSeconds;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final TicketGrantingTicketExpirationPolicy rhs = (TicketGrantingTicketExpirationPolicy) obj;
        return new EqualsBuilder()
                .append(this.maxTimeToLiveInSeconds, rhs.maxTimeToLiveInSeconds)
                .append(this.timeToKillInSeconds, rhs.timeToKillInSeconds)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.maxTimeToLiveInSeconds)
                .append(this.timeToKillInSeconds)
                .toHashCode();
    }
}
