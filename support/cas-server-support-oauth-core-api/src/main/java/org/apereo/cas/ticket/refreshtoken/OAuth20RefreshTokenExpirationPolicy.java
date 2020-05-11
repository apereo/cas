package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.expiration.AbstractCasExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.val;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

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
public class OAuth20RefreshTokenExpirationPolicy extends AbstractCasExpirationPolicy {

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
    public OAuth20RefreshTokenExpirationPolicy(@JsonProperty("timeToLive") final long timeToKillInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        val expired = isRefreshTokenExpired(ticketState);
        if (!expired) {
            return super.isExpired(ticketState);
        }
        return expired;
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

    /**
     * Is refresh token expired ?
     *
     * @param ticketState the ticket state
     * @return true/false
     */
    @JsonIgnore
    protected boolean isRefreshTokenExpired(final TicketState ticketState) {
        if (ticketState == null) {
            return true;
        }
        val expiringTime = ticketState.getCreationTime().plus(this.timeToKillInSeconds, ChronoUnit.SECONDS);
        return expiringTime.isBefore(ZonedDateTime.now(ZoneOffset.UTC));
    }

    /**
     * An expiration policy that is independent from the parent ticket-granting ticket.
     * Activated when refresh tokens are expected to live beyond the normal expiration policy
     * of the TGT that lent a hand in issuing them. If the refresh token is considered expired
     * by this policy, the parent ticket's expiration policy is not consulted, making the RT independent.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class OAuthRefreshTokenStandaloneExpirationPolicy extends OAuth20RefreshTokenExpirationPolicy {
        private static final long serialVersionUID = -7768661082888351104L;

        @JsonCreator
        public OAuthRefreshTokenStandaloneExpirationPolicy(@JsonProperty("timeToLive") final long timeToKillInSeconds) {
            super(timeToKillInSeconds);
        }

        @Override
        public boolean isExpired(final TicketState ticketState) {
            return isRefreshTokenExpired(ticketState);
        }
    }
}
