package org.apereo.cas.ticket.accesstoken;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.ticket.support.AbstractCasExpirationPolicy;
import org.apereo.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link OAuthAccessTokenExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuthAccessTokenExpirationPolicy extends AbstractCasExpirationPolicy {
    private static final long serialVersionUID = -8383186650682727360L;

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthAccessTokenExpirationPolicy.class);

    /** Maximum time this token is valid.  */
    private long maxTimeToLiveInMilliSeconds;

    /** Time to kill in milliseconds. */
    private long timeToKillInMilliSeconds;

    public OAuthAccessTokenExpirationPolicy() {}

    /**
     * Instantiates a new OAuth access token expiration policy.
     *
     * @param maxTimeToLive the max time to live
     * @param timeToKill the time to kill
     * @param timeUnit the time unit
     */
    public OAuthAccessTokenExpirationPolicy(final long maxTimeToLive, final long timeToKill, final TimeUnit timeUnit) {
        this.maxTimeToLiveInMilliSeconds = timeUnit.toMillis(maxTimeToLive);
        this.timeToKillInMilliSeconds = timeUnit.toMillis(timeToKill);
    }

    @JsonCreator
    public OAuthAccessTokenExpirationPolicy(@JsonProperty("timeToLive") final long maxTimeToLiveInMilliSeconds, 
                                            @JsonProperty("timeToIdle") final long timeToKillInMilliSeconds) {
        this.maxTimeToLiveInMilliSeconds = maxTimeToLiveInMilliSeconds;
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        final ZonedDateTime currentSystemTime = ZonedDateTime.now(ZoneOffset.UTC);
        final ZonedDateTime creationTime = ticketState.getCreationTime();

        // token has been used, check maxTimeToLive (hard window)
        ZonedDateTime expirationTime = creationTime.plus(this.maxTimeToLiveInMilliSeconds, ChronoUnit.MILLIS);
        if (currentSystemTime.isAfter(expirationTime)) {
            LOGGER.debug("Access token is expired because the time since creation is greater than maxTimeToLiveInMilliSeconds");
            return true;
        }

        // token is within hard window, check timeToKill (sliding window)
        expirationTime = creationTime.plus(this.timeToKillInMilliSeconds, ChronoUnit.MILLIS);
        if (ticketState.getLastTimeUsed().isAfter(expirationTime)) {
            LOGGER.debug("Access token is expired because the time since last use is greater than timeToKillInMilliseconds");
            return true;
        }

        return false;
    }

    @Override
    public Long getTimeToLive() {
        return this.maxTimeToLiveInMilliSeconds;
    }

    @Override
    public Long getTimeToIdle() {
        return this.timeToKillInMilliSeconds;
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final OAuthAccessTokenExpirationPolicy rhs = (OAuthAccessTokenExpirationPolicy) obj;
        return new EqualsBuilder()
                .append(this.maxTimeToLiveInMilliSeconds, rhs.maxTimeToLiveInMilliSeconds)
                .append(this.timeToKillInMilliSeconds, rhs.timeToKillInMilliSeconds)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(maxTimeToLiveInMilliSeconds)
                .append(timeToKillInMilliSeconds)
                .toHashCode();
    }
}
