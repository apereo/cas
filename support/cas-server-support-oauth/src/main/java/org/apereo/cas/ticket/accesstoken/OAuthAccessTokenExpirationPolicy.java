package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.support.AbstractCasExpirationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

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
    private long maxTimeToLiveInSeconds;

    /** Time to kill in milliseconds. */
    private long timeToKillInSeconds;

    public OAuthAccessTokenExpirationPolicy() {}

    /**
     * Instantiates a new OAuth access token expiration policy.
     *
     * @param maxTimeToLive the max time to live
     * @param timeToKill the time to kill
     */
    public OAuthAccessTokenExpirationPolicy(final long maxTimeToLive, final long timeToKill) {
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
        expirationTime = creationTime.plus(this.timeToKillInSeconds, ChronoUnit.SECONDS);
        if (ticketState.getLastTimeUsed().isAfter(expirationTime)) {
            LOGGER.debug("Access token is expired because the time since last use is greater than timeToKillInMilliseconds");
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
