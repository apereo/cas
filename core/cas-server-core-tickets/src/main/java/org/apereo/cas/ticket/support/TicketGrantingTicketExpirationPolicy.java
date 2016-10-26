package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.util.concurrent.TimeUnit;

/**
 * Provides the Ticket Granting Ticket expiration policy.  Ticket Granting Tickets
 * can be used any number of times, have a fixed lifetime, and an idle timeout.
 *
 * @author William G. Thompson, Jr.
 * @since 3.4.10
 */
public class TicketGrantingTicketExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = 7670537200691354820L;

    /**
     * The Logger instance for this class. Using a transient instance field for the Logger doesn't work, on object
     * deserialization the field is null.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketGrantingTicketExpirationPolicy.class);

    /** Maximum time this ticket is valid.  */
    private long maxTimeToLiveInMilliSeconds;

    /** Time to kill in milliseconds. */
    private long timeToKillInMilliSeconds;

    public TicketGrantingTicketExpirationPolicy() {}

    /**
     * Instantiates a new Ticket granting ticket expiration policy.
     *
     * @param maxTimeToLive the max time to live
     * @param timeToKill the time to kill
     * @param timeUnit the time unit
     */
    public TicketGrantingTicketExpirationPolicy(final long maxTimeToLive, final long timeToKill, final TimeUnit timeUnit) {
        this.maxTimeToLiveInMilliSeconds = timeUnit.toMillis(maxTimeToLive);
        this.timeToKillInMilliSeconds = timeUnit.toMillis(timeToKill);
    }

    @JsonCreator
    public TicketGrantingTicketExpirationPolicy(@JsonProperty("timeToLive") final long maxTimeToLiveInMilliSeconds, 
                                                @JsonProperty("timeToIdle") final long timeToKillInMilliSeconds) {
        this.maxTimeToLiveInMilliSeconds = maxTimeToLiveInMilliSeconds;
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
    }

    /**
     * After properties set.
     */
    @PostConstruct
    public void afterPropertiesSet() {
        Assert.isTrue(this.maxTimeToLiveInMilliSeconds >= this.timeToKillInMilliSeconds,
                "maxTimeToLiveInMilliSeconds must be greater than or equal to timeToKillInMilliSeconds.");
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        final ZonedDateTime currentSystemTime = ZonedDateTime.now(ZoneOffset.UTC);
        final ZonedDateTime creationTime = ticketState.getCreationTime();
        final ZonedDateTime lastTimeUsed = ticketState.getLastTimeUsed();
        
        // Ticket has been used, check maxTimeToLive (hard window)
        ZonedDateTime expirationTime = creationTime.plus(this.maxTimeToLiveInMilliSeconds, ChronoUnit.MILLIS);
        if (currentSystemTime.isAfter(expirationTime)) {
            LOGGER.debug("Ticket is expired because the time since creation is greater than maxTimeToLiveInMilliSeconds");
            return true;
        }

        expirationTime = lastTimeUsed.plus(this.timeToKillInMilliSeconds, ChronoUnit.MILLIS);
        if (currentSystemTime.isAfter(expirationTime)) {
            LOGGER.debug("Ticket is expired because the time since last use is greater than timeToKillInMilliseconds");
            return true;
        }

        return false;
    }

    @Override
    public Long getTimeToLive() {
        return this.maxTimeToLiveInMilliSeconds;
    }

    @Override
    public Long getTimeToIdle() {
        return this.timeToKillInMilliSeconds;
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
                .append(this.maxTimeToLiveInMilliSeconds, rhs.maxTimeToLiveInMilliSeconds)
                .append(this.timeToKillInMilliSeconds, rhs.timeToKillInMilliSeconds)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(maxTimeToLiveInMilliSeconds)
                .append(timeToKillInMilliSeconds)
                .toHashCode();
    }
}
