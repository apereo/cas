package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.ticket.TicketState;
import org.springframework.util.Assert;
import javax.annotation.PostConstruct;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import lombok.NoArgsConstructor;

/**
 * Provides the Ticket Granting Ticket expiration policy.  Ticket Granting Tickets
 * can be used any number of times, have a fixed lifetime, and an idle timeout.
 *
 * @author William G. Thompson, Jr.
 * @since 3.4.10
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
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

    /**
     * After properties set.
     */
    @PostConstruct
    public void afterPropertiesSet() {
        Assert.isTrue(this.maxTimeToLiveInSeconds >= this.timeToKillInSeconds, "maxTimeToLiveInSeconds must be greater than or equal to timeToKillInSeconds.");
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        final ZonedDateTime currentSystemTime = getCurrentSystemTime();
        final ZonedDateTime creationTime = ticketState.getCreationTime();
        final ZonedDateTime lastTimeUsed = ticketState.getLastTimeUsed();
        // Ticket has been used, check maxTimeToLive (hard window)
        ZonedDateTime expirationTime = creationTime.plus(this.maxTimeToLiveInSeconds, ChronoUnit.SECONDS);
        if (currentSystemTime.isAfter(expirationTime)) {
            LOGGER.debug("Ticket is expired because the time since creation [{}] is greater than current system time [{}]", expirationTime, currentSystemTime);
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

}
