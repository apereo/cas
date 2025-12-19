package org.apereo.cas.ticket.accesstoken;

import module java.base;
import org.apereo.cas.ticket.IdleExpirationPolicy;
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
 * This is {@link OAuth20AccessTokenExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OAuth20AccessTokenExpirationPolicy extends AbstractCasExpirationPolicy implements IdleExpirationPolicy {

    @Serial
    private static final long serialVersionUID = -8383186650682727360L;

    /**
     * Maximum time this token is valid.
     */
    private long maxTimeToLiveInSeconds;

    /**
     * Time to kill in seconds.
     */
    private long timeToKillInSeconds;

    @JsonCreator
    public OAuth20AccessTokenExpirationPolicy(
        @JsonProperty("timeToLive")
        final long maxTimeToLive,
        @JsonProperty("timeToIdle")
        final long timeToKill) {
        this.maxTimeToLiveInSeconds = maxTimeToLive;
        this.timeToKillInSeconds = timeToKill;
    }

    @Override
    public boolean isExpired(final TicketGrantingTicketAwareTicket ticketState) {
        val expired = isAccessTokenExpired(ticketState);
        return expired || super.isExpired(ticketState);
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

    @JsonIgnore
    protected boolean isAccessTokenExpired(final Ticket ticketState) {
        val currentSystemTime = ZonedDateTime.now(ZoneOffset.UTC);
        var expirationTime = toMaximumExpirationTime(ticketState);
        if (currentSystemTime.isAfter(expirationTime)) {
            LOGGER.debug("Access token is expired because the current time [{}] is after [{}]", currentSystemTime, expirationTime);
            return true;
        }

        val expirationTimeToKill = getIdleExpirationTime(ticketState);
        if (currentSystemTime.isAfter(expirationTimeToKill)) {
            LOGGER.debug("Access token is expired because the current time [{}] is after [{}]", currentSystemTime, expirationTimeToKill);
            return true;
        }
        return false;
    }

    /**
     * An expiration policy that is independent from the parent ticket-granting ticket.
     * Activated when access tokens are expected to live beyond the normal expiration policy
     * of the TGT that lent a hand in issuing them. If the access token is considered expired
     * by this policy, the parent ticket's expiration policy is not consulted, making the AT independent.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class OAuthAccessTokenSovereignExpirationPolicy extends OAuth20AccessTokenExpirationPolicy {
        @Serial
        private static final long serialVersionUID = -7768661082888351104L;

        @JsonCreator
        public OAuthAccessTokenSovereignExpirationPolicy(
            @JsonProperty("timeToLive")
            final long maxTimeToLive,
            @JsonProperty("timeToIdle")
            final long timeToKill) {
            super(maxTimeToLive, timeToKill);
        }

        @Override
        public boolean isExpired(final TicketGrantingTicketAwareTicket ticketState) {
            return isAccessTokenExpired(ticketState);
        }
    }
}
