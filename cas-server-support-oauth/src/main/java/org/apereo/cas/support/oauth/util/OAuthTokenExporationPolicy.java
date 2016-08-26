package org.apereo.cas.support.oauth.util;

import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.support.AbstractCasExpirationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * OAuth2 Token expiration policy
 * <p>
 * Created by ZhangZhenli on 2016/8/26.
 */
public class OAuthTokenExporationPolicy extends AbstractCasExpirationPolicy {

    private static final long serialVersionUID = 7710333719326016754L;

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthTokenExporationPolicy.class);

    /**
     * Maximum number of use
     */
    private int numberOfUses;

    /**
     * Time to kill in milliseconds.
     */
    private long timeToKillInMilliSeconds;

    /**
     * Maximum time this token is valid.
     */
    private long maxTimeToLiveInMilliSeconds;

    /**
     * No-arg constructor for serialization support.
     */
    private OAuthTokenExporationPolicy() {
        this.numberOfUses = -1;
        this.timeToKillInMilliSeconds = -1;
        this.maxTimeToLiveInMilliSeconds = -1;
    }


    /**
     * Instantiates a new multi time use or timeout expiration policy.
     *
     * @param numberOfUses                the number of uses
     * @param timeToKillInMilliSeconds    the time to kill in milli seconds
     * @param maxTimeToLiveInMilliSeconds Maximum time this token is valid.
     */
    public OAuthTokenExporationPolicy(final int numberOfUses,
                                      final long timeToKillInMilliSeconds, final long maxTimeToLiveInMilliSeconds) {
        this.numberOfUses = numberOfUses;
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
        this.maxTimeToLiveInMilliSeconds = maxTimeToLiveInMilliSeconds;
    }

    /**
     * Instantiates a new multi time use or timeout expiration policy.
     *
     * @param numberOfUses  the number of uses
     * @param timeToKill    the time to kill
     * @param maxTimeToLive the maximum time to kill
     * @param timeUnit      the time unit
     */
    public OAuthTokenExporationPolicy(final int numberOfUses, final long timeToKill, final long maxTimeToLive,
                                      final TimeUnit timeUnit) {
        this(numberOfUses, timeUnit.toMillis(timeToKill), timeUnit.toMillis(maxTimeToLive));
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        if (ticketState == null) {
            LOGGER.debug("Ticket state is null for {}", this.getClass().getSimpleName());
            return true;
        }
        if (this.numberOfUses >= 0) {
            final long countUses = ticketState.getCountOfUses();
            if (countUses >= this.numberOfUses) {
                LOGGER.debug("Ticket usage count {} is greater than or equal to {}", countUses, this.numberOfUses);
                return true;
            }
        }

        final ZonedDateTime currentSystemTime = ZonedDateTime.now(ZoneOffset.UTC);
        final ZonedDateTime creationTime = ticketState.getCreationTime();

        // token has been used, check maxTimeToLive (hard window)
        if (this.maxTimeToLiveInMilliSeconds >= 0) {
            ZonedDateTime expirationTime = creationTime.plus(this.maxTimeToLiveInMilliSeconds, ChronoUnit.MILLIS);
            if (currentSystemTime.isAfter(expirationTime)) {
                LOGGER.debug("Access token is expired because the time since creation is greater than maxTimeToLiveInMilliSeconds");
                return true;
            }
        }

        // token is within hard window, check timeToKill (sliding window)
        if (this.timeToKillInMilliSeconds >= 0) {
            ZonedDateTime expirationTime = creationTime.plus(this.timeToKillInMilliSeconds, ChronoUnit.MILLIS);
            if (ticketState.getLastTimeUsed().isAfter(expirationTime)) {
                LOGGER.debug("Access token is expired because the time since last use is greater than timeToKillInMilliseconds");
                return true;
            }
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

}
