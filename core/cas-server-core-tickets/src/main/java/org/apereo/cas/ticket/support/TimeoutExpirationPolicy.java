package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.ticket.TicketState;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * Expiration policy that is based on a certain time period for a ticket to
 * exist.
 * <p>
 * The expiration policy defined by this class is one of inactivity.  If you are inactive for the specified
 * amount of time, the ticket will be expired.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = -7636642464326939536L;

    /** The time to kill in milliseconds. */
    private long timeToKillInMilliSeconds;


    /** No-arg constructor for serialization support. */
    public TimeoutExpirationPolicy() {
        this.timeToKillInMilliSeconds = 0;
    }

    /**
     * Instantiates a new timeout expiration policy.
     *
     * @param timeToKillInMilliSeconds the time to kill in milli seconds
     */
    @JsonCreator
    public TimeoutExpirationPolicy(@JsonProperty("timeToIdle") final long timeToKillInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
    }

    /**
     * Instantiates a new Timeout expiration policy.
     *
     * @param timeToKill the time to kill
     * @param timeUnit the time unit
     */
    public TimeoutExpirationPolicy(final long timeToKill, final TimeUnit timeUnit) {
        this.timeToKillInMilliSeconds = timeUnit.toMillis(timeToKill);
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        final ZonedDateTime expirationTime = now.plus(this.timeToKillInMilliSeconds, ChronoUnit.MILLIS);
        return ticketState == null || now.isAfter(expirationTime);
    }

    @JsonIgnore
    @Override
    public Long getTimeToLive() {
        return new Long(Integer.MAX_VALUE);
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
        final TimeoutExpirationPolicy rhs = (TimeoutExpirationPolicy) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.timeToKillInMilliSeconds, rhs.timeToKillInMilliSeconds)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(timeToKillInMilliSeconds)
                .toHashCode();
    }
}
