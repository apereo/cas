package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.ticket.TicketState;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Implementation of an expiration policy that adds the concept of saying that a
 * ticket can only be used once every X milliseconds to prevent mis-configured
 * clients from consuming resources by doing constant redirects.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@Slf4j
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ThrottledUseAndTimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = 205979491183779408L;

    /** The time to kill in seconds. */
    private long timeToKillInSeconds;

    private long timeInBetweenUsesInSeconds;

    @JsonCreator
    public ThrottledUseAndTimeoutExpirationPolicy(@JsonProperty("timeToLive") final long timeToKillInSeconds, @JsonProperty("timeToIdle") final long timeInBetweenUsesInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
        this.timeInBetweenUsesInSeconds = timeInBetweenUsesInSeconds;
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        final ZonedDateTime currentTime = ZonedDateTime.now(ZoneOffset.UTC);
        final ZonedDateTime lastTimeUsed = ticketState.getLastTimeUsed();
        final ZonedDateTime killTime = lastTimeUsed.plus(this.timeToKillInSeconds, ChronoUnit.SECONDS);
        if (ticketState.getCountOfUses() == 0 && currentTime.isBefore(killTime)) {
            LOGGER.debug("Ticket is not expired due to a count of zero and the time being less " + "than the timeToKillInSeconds");
            return false;
        }
        if (currentTime.isAfter(killTime)) {
            LOGGER.debug("Ticket is expired due to the time being greater than the timeToKillInSeconds");
            return true;
        }
        final ZonedDateTime dontUseUntil = lastTimeUsed.plus(this.timeInBetweenUsesInSeconds, ChronoUnit.SECONDS);
        if (currentTime.isBefore(dontUseUntil)) {
            LOGGER.warn("Ticket is expired due to the time being less than the waiting period.");
            return true;
        }
        return false;
    }

    @Override
    public Long getTimeToLive() {
        return this.timeToKillInSeconds;
    }

    @Override
    public Long getTimeToIdle() {
        return this.timeInBetweenUsesInSeconds;
    }

}
