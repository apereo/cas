package org.apereo.cas.ticket.expiration;

import org.apereo.cas.ticket.TicketState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.val;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Ticket expiration policy based on a hard timeout from ticket creation time rather than the
 * "idle" timeout provided by {@link TimeoutExpirationPolicy}.
 *
 * @author Andrew Feller
 * @since 3.1.2
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
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
        if (ticketState == null) {
            return true;
        }
        val expiringTime = ticketState.getCreationTime().plus(this.timeToKillInSeconds, ChronoUnit.SECONDS);
        val expired = expiringTime.isBefore(ZonedDateTime.now(getClock()));
        return expired || super.isExpired(ticketState);
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

}
