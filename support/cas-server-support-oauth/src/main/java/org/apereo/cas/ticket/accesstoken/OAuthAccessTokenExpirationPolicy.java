package org.apereo.cas.ticket.accesstoken;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.support.AbstractCasExpirationPolicy;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import lombok.NoArgsConstructor;

/**
 * This is {@link OAuthAccessTokenExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
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

    /**
     * Instantiates a new OAuth access token expiration policy.
     *
     * @param maxTimeToLive the max time to live
     * @param timeToKill    the time to kill
     */
    @JsonCreator
    public OAuthAccessTokenExpirationPolicy(@JsonProperty("timeToLive") final long maxTimeToLive, @JsonProperty("timeToIdle") final long timeToKill) {
        this.maxTimeToLiveInSeconds = maxTimeToLive;
        this.timeToKillInSeconds = timeToKill;
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        final ZonedDateTime currentSystemTime = ZonedDateTime.now(ZoneOffset.UTC);
        final ZonedDateTime creationTime = ticketState.getCreationTime();
        // token has been used, check maxTimeToLive (hard window)
        ZonedDateTime expirationTime = creationTime.plus(this.maxTimeToLiveInSeconds, ChronoUnit.SECONDS);
        if (currentSystemTime.isAfter(expirationTime)) {
            LOGGER.debug("Access token is expired because the time since creation is greater than maxTimeToLiveInSeconds");
            return true;
        }
        // token is within hard window, check timeToKill (sliding window)
        expirationTime = ticketState.getLastTimeUsed().plus(this.timeToKillInSeconds, ChronoUnit.SECONDS);
        if (currentSystemTime.isAfter(expirationTime)) {
            LOGGER.debug("Access token is expired because the time since last use is greater than timeToKillInSeconds");
            return true;
        }
        return false;
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
