package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apereo.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Implementation of an expiration policy that adds the concept of saying that a
 * ticket can only be used once every X milliseconds to prevent misconfigured
 * clients from consuming resources by doing constant redirects.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class ThrottledUseAndTimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = 205979491183779408L;

    /**
     * The Logger instance for this class. Using a transient instance field for the Logger doesn't work, on object
     * deserialization the field is null.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ThrottledUseAndTimeoutExpirationPolicy.class);

    /** The time to kill in milliseconds. */
    private long timeToKillInMilliSeconds;

    private long timeInBetweenUsesInMilliSeconds;

    /**
     * Instantiates a new Throttled use and timeout expiration policy.
     */
    public ThrottledUseAndTimeoutExpirationPolicy(){}

    @JsonCreator
    public ThrottledUseAndTimeoutExpirationPolicy(@JsonProperty("timeToLive") final long timeToKillInMilliSeconds, @JsonProperty("timeToIdle") final long timeInBetweenUsesInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
        this.timeInBetweenUsesInMilliSeconds = timeInBetweenUsesInMilliSeconds;
    }

    public void setTimeInBetweenUsesInMilliSeconds(final long timeInBetweenUsesInMilliSeconds) {
        this.timeInBetweenUsesInMilliSeconds = timeInBetweenUsesInMilliSeconds;
    }

    public void setTimeToKillInMilliSeconds(final long timeToKillInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        final ZonedDateTime currentTime = ZonedDateTime.now(ZoneOffset.UTC);
        final ZonedDateTime lastTimeUsed = ticketState.getLastTimeUsed();
        final ZonedDateTime killTime = lastTimeUsed.plus(this.timeToKillInMilliSeconds, ChronoUnit.MILLIS);

        if (ticketState.getCountOfUses() == 0 && currentTime.isBefore(killTime)) {
            LOGGER.debug("Ticket is not expired due to a count of zero and the time being less "
                    + "than the timeToKillInMilliseconds");
            return false;
        }

        if (currentTime.isAfter(killTime)) {
            LOGGER.debug("Ticket is expired due to the time being greater than the timeToKillInMilliseconds");
            return true;
        }

        final ZonedDateTime dontUseUntil = lastTimeUsed.plus(this.timeInBetweenUsesInMilliSeconds, ChronoUnit.MILLIS);
        if (currentTime.isBefore(dontUseUntil)) {
            LOGGER.warn("Ticket is expired due to the time being less than the waiting period.");
            return true;
        }

        return false;
    }

    @Override
    public Long getTimeToLive() {
        return this.timeToKillInMilliSeconds;
    }

    @Override
    public Long getTimeToIdle() {
        return this.timeInBetweenUsesInMilliSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThrottledUseAndTimeoutExpirationPolicy that = (ThrottledUseAndTimeoutExpirationPolicy) o;

        if (timeToKillInMilliSeconds != that.timeToKillInMilliSeconds) return false;
        return timeInBetweenUsesInMilliSeconds == that.timeInBetweenUsesInMilliSeconds;
    }

    @Override
    public int hashCode() {
        int result = (int) (timeToKillInMilliSeconds ^ (timeToKillInMilliSeconds >>> 32));
        result = 31 * result + (int) (timeInBetweenUsesInMilliSeconds ^ (timeInBetweenUsesInMilliSeconds >>> 32));
        return result;
    }
}
