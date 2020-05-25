package org.apereo.cas.ticket.expiration;

import org.apereo.cas.ticket.TicketState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Implementation of an expiration policy that adds the concept of saying that a
 * ticket can only be used once every X milliseconds to prevent mis-configured
 * clients from consuming resources by doing constant redirects.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ThrottledUseAndTimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    /**
     * Serialization support.
     */
    private static final long serialVersionUID = 205979491183779408L;

    /**
     * The time to kill in seconds.
     */
    private long timeToKillInSeconds;

    private long timeInBetweenUsesInSeconds;

    @JsonCreator
    public ThrottledUseAndTimeoutExpirationPolicy(@JsonProperty("timeToLive") final long timeToKillInSeconds,
                                                  @JsonProperty("timeToIdle") final long timeInBetweenUsesInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
        this.timeInBetweenUsesInSeconds = timeInBetweenUsesInSeconds;
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        LOGGER.trace("Checking validity of ticket [{}]", ticketState);
        val lastTimeUsed = ticketState.getLastTimeUsed();
        val currentTime = ZonedDateTime.now(getClock());

        LOGGER.trace("Current time is [{}]. Ticket last used time is [{}]", currentTime, lastTimeUsed);

        val margin = Duration.between(lastTimeUsed, currentTime).toSeconds();

        LOGGER.trace("The duration in seconds between current time and last used time is [{}]", margin);

        if (ticketState.getCountOfUses() == 0 && margin < this.timeToKillInSeconds) {
            LOGGER.debug("Valid [{}]: Usage count is zero and number of seconds since ticket usage time [{}] is less than [{}]",
                ticketState, margin, this.timeToKillInSeconds);
            return super.isExpired(ticketState);
        }

        if (margin >= this.timeToKillInSeconds) {
            LOGGER.debug("Expired [{}]: number of seconds since ticket usage time [{}] is greater than or equal to [{}]",
                ticketState, margin, this.timeToKillInSeconds);
            return true;
        }
        if (margin > 0 && margin <= this.timeInBetweenUsesInSeconds) {
            LOGGER.warn("Expired [{}]: number of seconds since ticket usage time [{}] is less than or equal to time in between uses in seconds [{}]",
                ticketState, margin, this.timeInBetweenUsesInSeconds);
            return true;
        }

        return super.isExpired(ticketState);
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
