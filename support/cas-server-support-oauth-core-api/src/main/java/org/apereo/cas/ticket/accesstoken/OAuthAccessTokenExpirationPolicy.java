package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.support.AbstractCasExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * This is {@link OAuthAccessTokenExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OAuthAccessTokenExpirationPolicy extends AbstractCasExpirationPolicy {

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
    public OAuthAccessTokenExpirationPolicy(@JsonProperty("timeToLive") final long maxTimeToLive,
                                            @JsonProperty("timeToIdle") final long timeToKill) {
        this.maxTimeToLiveInSeconds = maxTimeToLive;
        this.timeToKillInSeconds = timeToKill;
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        val expired = isAccessTokenExpired(ticketState);
        if (!expired) {
            return super.isExpired(ticketState);
        }
        return expired;
    }

    @Override
    public Long getTimeToLive() {
        return this.maxTimeToLiveInSeconds;
    }

    @Override
    public Long getTimeToIdle() {
        return this.timeToKillInSeconds;
    }

    /**
     * Is access token expired ?.
     *
     * @param ticketState the ticket state
     * @return the boolean
     */
    @JsonIgnore
    protected boolean isAccessTokenExpired(final TicketState ticketState) {
        val currentSystemTime = ZonedDateTime.now(ZoneOffset.UTC);
        val creationTime = ticketState.getCreationTime();

        var expirationTime = creationTime.plus(this.maxTimeToLiveInSeconds, ChronoUnit.SECONDS);
        if (currentSystemTime.isAfter(expirationTime)) {
            LOGGER.debug("Access token is expired because the current time [{}] is after [{}]", currentSystemTime, expirationTime);
            return true;
        }

        val expirationTimeToKill = ticketState.getLastTimeUsed().plus(this.timeToKillInSeconds, ChronoUnit.SECONDS);
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
    @Slf4j
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class OAuthAccessTokenSovereignExpirationPolicy extends OAuthAccessTokenExpirationPolicy {
        private static final long serialVersionUID = -7768661082888351104L;

        @JsonCreator
        public OAuthAccessTokenSovereignExpirationPolicy(@JsonProperty("timeToLive") final long maxTimeToLive,
                                                         @JsonProperty("timeToIdle") final long timeToKill) {
            super(maxTimeToLive, timeToKill);
        }

        @Override
        public boolean isExpired(final TicketState ticketState) {
            return isAccessTokenExpired(ticketState);
        }
    }
}
