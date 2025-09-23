package org.apereo.cas.ticket.expiration;


import org.apereo.cas.ticket.IdleExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serial;
import java.time.Clock;
import java.time.ZonedDateTime;

/**
 * Expiration policy that is based on a certain time period for a ticket to
 * exist. The expiration policy defined by this class is one of inactivity.  If you are inactive for the specified
 * amount of time, the ticket will be expired.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Slf4j
public class TimeoutExpirationPolicy extends AbstractCasExpirationPolicy implements IdleExpirationPolicy {
    private static final long MAX_EXPIRATION_IN_YEARS = 50L;
    
    @Serial
    private static final long serialVersionUID = -7636642464326939536L;

    /**
     * The time to kill in seconds.
     */
    private long timeToKillInSeconds;

    @JsonCreator
    public TimeoutExpirationPolicy(
        @JsonProperty("timeToIdle") final long timeToKillInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
    }

    @Override
    public boolean isExpired(final TicketGrantingTicketAwareTicket ticketState) {
        if (ticketState == null) {
            return true;
        }
        val now = ZonedDateTime.now(getClock());
        val expirationTime = getIdleExpirationTime(ticketState);
        val expired = now.isAfter(expirationTime);
        val result = expired || super.isExpired(ticketState);
        if (result) {
            LOGGER.trace("Ticket [{}] is expired because its expiration time [{}] is after [{}] or its parent ticket, if any, has expired",
                ticketState.getId(), expirationTime, now);
        }
        return result;
    }

    @JsonIgnore
    @Override
    public Long getTimeToLive() {
        return Long.MAX_VALUE;
    }

    @Override
    public Long getTimeToIdle() {
        return this.timeToKillInSeconds;
    }

    @JsonIgnore
    @Override
    public ZonedDateTime getIdleExpirationTime(final Ticket ticketState) {
        val lastTimeUsed = ticketState.getLastTimeUsed();
        return lastTimeUsed.plusSeconds(this.timeToKillInSeconds);
    }

    @Override
    public ZonedDateTime toMaximumExpirationTime(final Ticket ticketState) {
        return ZonedDateTime.now(Clock.systemUTC()).plusYears(MAX_EXPIRATION_IN_YEARS);
    }
}
