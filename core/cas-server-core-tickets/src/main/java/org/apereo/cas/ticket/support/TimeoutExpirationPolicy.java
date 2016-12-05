package org.apereo.cas.ticket.support;

import org.apereo.cas.ticket.TicketState;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Expiration policy that is based on a certain time period for a ticket to
 * exist.
 * <p>
 * The expiration policy defined by this class is one of inactivity.  If you are inactive for the specified
 * amount of time, the ticket will be expired.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class TimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = -7636642464326939536L;

    /** The time to kill in seconds. */
    private final long timeToKillInSeconds;


    /** No-arg constructor for serialization support. */
    public TimeoutExpirationPolicy() {
        this.timeToKillInSeconds = 0;
    }

    /**
     * Instantiates a new timeout expiration policy.
     *
     * @param timeToKillInSeconds the time to kill in seconds
     */
    public TimeoutExpirationPolicy(final long timeToKillInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
    }


    @Override
    public boolean isExpired(final TicketState ticketState) {
        if (ticketState == null) {
            return true;
        }
        final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        final ZonedDateTime expirationTime = ticketState.getLastTimeUsed().plus(this.timeToKillInSeconds, ChronoUnit.SECONDS);
        return now.isAfter(expirationTime);
    }

    @Override
    public Long getTimeToLive() {
        return new Long(Integer.MAX_VALUE);
    }

    @Override
    public Long getTimeToIdle() {
        return this.timeToKillInSeconds;
    }
}
