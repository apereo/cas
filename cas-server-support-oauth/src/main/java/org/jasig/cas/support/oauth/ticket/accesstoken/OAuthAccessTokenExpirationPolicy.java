package org.jasig.cas.support.oauth.ticket.accesstoken;

import org.jasig.cas.ticket.TicketState;
import org.jasig.cas.ticket.support.AbstractCasExpirationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link OAuthAccessTokenExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("oAuthAcccessTokenExpirationPolicy")
public class OAuthAccessTokenExpirationPolicy extends AbstractCasExpirationPolicy {
    private static final long serialVersionUID = -8383186650682727360L;

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthAccessTokenExpirationPolicy.class);
    /** Maximum time this ticket is valid.  */
    @Value("#{${oauth.access.token.maxTimeToLiveInSeconds:28800}*1000}")
    private long maxTimeToLiveInMilliSeconds;

    /** Time to kill in milliseconds. */
    @Value("#{${oauth.access.token.timeToKillInSeconds:7200}*1000}")
    private long timeToKillInMilliSeconds;

    private OAuthAccessTokenExpirationPolicy() {}

    /**
     * Instantiates a new Ticket granting ticket expiration policy.
     *
     * @param maxTimeToLive the max time to live
     * @param timeToKill the time to kill
     * @param timeUnit the time unit
     */
    public OAuthAccessTokenExpirationPolicy(final long maxTimeToLive, final long timeToKill, final TimeUnit timeUnit) {
        this.maxTimeToLiveInMilliSeconds = timeUnit.toMillis(maxTimeToLive);
        this.timeToKillInMilliSeconds = timeUnit.toMillis(timeToKill);
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        final ZonedDateTime currentSystemTime = ZonedDateTime.now(ZoneOffset.UTC);
        final ZonedDateTime creationTime = ticketState.getCreationTime();

        // Ticket has been used, check maxTimeToLive (hard window)
        ZonedDateTime expirationTime = creationTime.plus(maxTimeToLiveInMilliSeconds, ChronoUnit.MILLIS);
        if (currentSystemTime.isAfter(expirationTime)) {
            LOGGER.debug("Access token is expired because the time since creation is greater than maxTimeToLiveInMilliSeconds");
            return true;
        }

        // Ticket is within hard window, check timeToKill (sliding window)
        expirationTime = creationTime.plus(timeToKillInMilliSeconds, ChronoUnit.MILLIS);
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
}
