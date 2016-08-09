package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.support.AbstractCasExpirationPolicy;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * This is OAuth refresh token expiration policy (max time to live = 1 month by default).
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public class OAuthRefreshTokenExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = -7144233906843566234L;

    /** The time to kill in milliseconds. */
    private long timeToKillInMilliSeconds;

    /** No-arg constructor for serialization support. */
    public OAuthRefreshTokenExpirationPolicy() {}

    /**
     * Instantiates a new OAuth refresh token expiration policy.
     *
     * @param timeToKillInMilliSeconds the time to kill in milli seconds
     */
    public OAuthRefreshTokenExpirationPolicy(final long timeToKillInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        return ticketState == null || ticketState.getCreationTime()
                .plus(this.timeToKillInMilliSeconds, ChronoUnit.MILLIS).isBefore(ZonedDateTime.now(ZoneOffset.UTC));
    }

    @Override
    public Long getTimeToLive() {
        return this.timeToKillInMilliSeconds;
    }

    @Override
    public Long getTimeToIdle() {
        return 0L;
    }
}
