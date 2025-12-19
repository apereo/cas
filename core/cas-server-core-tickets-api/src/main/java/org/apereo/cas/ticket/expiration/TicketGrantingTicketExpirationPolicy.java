package org.apereo.cas.ticket.expiration;

import module java.base;
import org.apereo.cas.ticket.IdleExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.Assert;

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
public class TicketGrantingTicketExpirationPolicy extends AbstractCasExpirationPolicy implements IdleExpirationPolicy {

    @Serial
    private static final long serialVersionUID = 7670537200691354820L;

    /**
     * Maximum time this ticket is valid.
     */
    private long maxTimeToLiveInSeconds;

    /**
     * Time to kill in seconds.
     */
    private long timeToKillInSeconds;

    @JsonCreator
    public TicketGrantingTicketExpirationPolicy(
        @JsonProperty("timeToLive") final long maxTimeToLive,
        @JsonProperty("timeToIdle") final long timeToKill) {
        this.maxTimeToLiveInSeconds = maxTimeToLive;
        this.timeToKillInSeconds = timeToKill;
    }


    @Override
    public boolean isExpired(final TicketGrantingTicketAwareTicket ticketState) {
        Assert.isTrue(this.maxTimeToLiveInSeconds >= this.timeToKillInSeconds,
            "maxTimeToLiveInSeconds must be greater than or equal to timeToKillInSeconds.");
        val currentSystemTime = ZonedDateTime.now(getClock());

        val expirationTime = toMaximumExpirationTime(ticketState);
        if (currentSystemTime.isAfter(expirationTime)) {
            LOGGER.debug("Ticket is expired because the time since creation [{}] is greater than current system time [{}]", expirationTime, currentSystemTime);
            return true;
        }
        val expirationTimeKill = getIdleExpirationTime(ticketState);
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

    @JsonIgnore
    @Override
    public ZonedDateTime toMaximumExpirationTime(final Ticket ticketState) {
        val creationTime = ticketState.getCreationTime();
        return creationTime.plusSeconds(this.maxTimeToLiveInSeconds);
    }

    @JsonIgnore
    @Override
    public ZonedDateTime getIdleExpirationTime(final Ticket ticketState) {
        val lastTimeUsed = ticketState.getLastTimeUsed();
        return lastTimeUsed.plusSeconds(this.timeToKillInSeconds);
    }
}
