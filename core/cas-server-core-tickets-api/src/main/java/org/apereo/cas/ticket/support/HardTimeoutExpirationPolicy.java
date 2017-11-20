package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.ticket.TicketState;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Ticket expiration policy based on a hard timeout from ticket creation time rather than the
 * "idle" timeout provided by {@link TimeoutExpirationPolicy}.
 *
 * @author Andrew Feller
 * @since 3.1.2
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class HardTimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    /**
     * Serialization support.
     */
    private static final long serialVersionUID = 6728077010285422290L;

    /**
     * The time to kill in seconds.
     */
    private long timeToKillInSeconds;

    /**
     * No-arg constructor for serialization support.
     */
    public HardTimeoutExpirationPolicy() {
    }

    /**
     * Instantiates a new hard timeout expiration policy.
     *
     * @param timeToKillInSeconds the time to kill in seconds
     */
    @JsonCreator
    public HardTimeoutExpirationPolicy(@JsonProperty("timeToLive") final long timeToKillInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        return ticketState == null || ticketState.getCreationTime()
                .plus(this.timeToKillInSeconds, ChronoUnit.SECONDS).isBefore(ZonedDateTime.now(ZoneOffset.UTC));
    }

    @Override
    public Long getTimeToLive() {
        return this.timeToKillInSeconds;
    }

    @JsonIgnore
    @Override
    public Long getTimeToIdle() {
        return 0L;
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
        final HardTimeoutExpirationPolicy rhs = (HardTimeoutExpirationPolicy) obj;
        return new EqualsBuilder()
                .append(this.timeToKillInSeconds, rhs.timeToKillInSeconds)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(timeToKillInSeconds)
                .toHashCode();
    }
}
