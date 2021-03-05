package org.apereo.cas.ticket.expiration;

import org.apereo.cas.ticket.TicketState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.Assert;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Provides the Ticket Granting Ticket expiration policy.  Ticket Granting Tickets
 * can be used any number of times, have a fixed lifetime, and an idle timeout.
 *
 * @author William G. Thompson, Jr.
 * @since 3.4.10
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
public class TicketGrantingTicketExpirationPolicy extends AbstractCasExpirationPolicy {

    /**
     * Serialization support.
     */
    private static final long serialVersionUID = 7670537200691354820L;

    /**
     * Maximum time this ticket is valid.
     */
    private long maxTimeToLiveInSeconds;

    /**
     * Time to kill in seconds.
     */
    private long timeToKillInSeconds;

    /**
     * Instantiates a new Ticket granting ticket expiration policy.
     *
     * @param maxTimeToLive the max time to live
     * @param timeToKill    the time to kill
     */
    @JsonCreator
    public TicketGrantingTicketExpirationPolicy(@JsonProperty("timeToLive") final long maxTimeToLive, @JsonProperty("timeToIdle") final long timeToKill) {
        this.maxTimeToLiveInSeconds = maxTimeToLive;
        this.timeToKillInSeconds = timeToKill;
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        Assert.isTrue(this.maxTimeToLiveInSeconds >= this.timeToKillInSeconds,
            "maxTimeToLiveInSeconds must be greater than or equal to timeToKillInSeconds.");

        val currentSystemTime = ZonedDateTime.now(getClock());
        val creationTime = ticketState.getCreationTime();
        val lastTimeUsed = ticketState.getLastTimeUsed();
        val expirationTime = creationTime.plus(this.maxTimeToLiveInSeconds, ChronoUnit.SECONDS);
        if (currentSystemTime.isAfter(expirationTime)) {
            LOGGER.debug("Ticket is expired because the time since creation [{}] is greater than current system time [{}]", expirationTime, currentSystemTime);
            return true;
        }
        val expirationTimeKill = lastTimeUsed.plus(this.timeToKillInSeconds, ChronoUnit.SECONDS);
        if (currentSystemTime.isAfter(expirationTimeKill)) {
            LOGGER.debug("Ticket is expired because the time since last use is greater than timeToKillInSeconds");
            return true;
        }
        return super.isExpired(ticketState);
    }

    @Override
    public Long getTimeToLive() {
        return this.maxTimeToLiveInSeconds;
    }

    @Override
    public Long getTimeToIdle() {
        return this.timeToKillInSeconds;
    }

}
