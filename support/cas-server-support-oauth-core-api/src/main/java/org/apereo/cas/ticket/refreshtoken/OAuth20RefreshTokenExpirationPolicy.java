package org.apereo.cas.ticket.refreshtoken;

import module java.base;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;
import org.apereo.cas.ticket.expiration.AbstractCasExpirationPolicy;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is OAuth refresh token expiration policy (max time to live = 1 month by default).
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Slf4j
public class OAuth20RefreshTokenExpirationPolicy extends AbstractCasExpirationPolicy {

    @Serial
    private static final long serialVersionUID = -7144233906843566234L;

    /**
     * The time to kill in milliseconds.
     */
    private long timeToKillInSeconds;

    /**
     * Instantiates a new OAuth refresh token expiration policy.
     *
     * @param timeToKillInSeconds the time to kill in seconds
     */
    @JsonCreator
    public OAuth20RefreshTokenExpirationPolicy(
        @JsonProperty("timeToLive")
        final long timeToKillInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
    }

    @Override
    public boolean isExpired(final TicketGrantingTicketAwareTicket ticketState) {
        val expired = isRefreshTokenExpired(ticketState);
        return expired || super.isExpired(ticketState);
    }

    @Override
    public Long getTimeToLive() {
        return this.timeToKillInSeconds;
    }
    
    /**
     * Is refresh token expired ?
     *
     * @param ticketState the ticket state
     * @return true/false
     */
    @JsonIgnore
    protected boolean isRefreshTokenExpired(final Ticket ticketState) {
        if (ticketState == null) {
            return true;
        }
        val expiringTime = toMaximumExpirationTime(ticketState);
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        val result = expiringTime.isBefore(now);
        if (result) {
            LOGGER.trace("Ticket [{}] is expired because its expiration time [{}] is before [{}]",
                ticketState.getId(), expiringTime, now);
        }
        return result;
    }

    @JsonIgnore
    @Override
    public ZonedDateTime toMaximumExpirationTime(final Ticket ticketState) {
        val creationTime = ticketState.getCreationTime();
        return creationTime.plusSeconds(this.timeToKillInSeconds);
    }

}
