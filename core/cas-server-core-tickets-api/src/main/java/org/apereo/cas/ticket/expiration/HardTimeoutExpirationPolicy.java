package org.apereo.cas.ticket.expiration;

import module java.base;
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
@Slf4j
public class HardTimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    @Serial
    private static final long serialVersionUID = 6728077010285422290L;

    private long timeToKillInSeconds;

    @JsonCreator
    public HardTimeoutExpirationPolicy(@JsonProperty("timeToLive") final long timeToKillInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
    }

    @Override
    public boolean isExpired(final TicketGrantingTicketAwareTicket ticketState) {
        if (ticketState == null) {
            return true;
        }
        val expiringTime = toMaximumExpirationTime(ticketState);
        val now = ZonedDateTime.now(getClock());
        val expired = expiringTime.isBefore(now);
        val result = expired || super.isExpired(ticketState);
        if (result) {
            LOGGER.trace("Ticket [{}] is expired because its expiration time [{}] is before [{}] or its parent ticket, if any, has expired",
                ticketState.getId(), expiringTime, now);
        }
        return result;
    }

    @Override
    public Long getTimeToLive() {
        return this.timeToKillInSeconds;
    }
    
    @JsonIgnore
    @Override
    public ZonedDateTime toMaximumExpirationTime(final Ticket ticketState) {
        val creationTime = ticketState.getCreationTime();
        return creationTime.plusSeconds(this.timeToKillInSeconds);
    }
}
