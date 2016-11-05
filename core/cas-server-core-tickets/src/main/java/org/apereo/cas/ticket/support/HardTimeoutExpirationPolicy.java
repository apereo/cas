package org.apereo.cas.ticket.support;

import org.apereo.cas.ticket.TicketState;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Ticket expiration policy based on a hard timeout from ticket creation time rather than the
 * "idle" timeout provided by {@link TimeoutExpirationPolicy}.
 *
 * @author Andrew Feller
 * @since 3.1.2
 */
public class HardTimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = 6728077010285422290L;

    /** The time to kill in seconds. */
    private long timeToKillInSeconds;

    /** No-arg constructor for serialization support. */
    public HardTimeoutExpirationPolicy() {}


    /**
     * Instantiates a new hard timeout expiration policy.
     *
     * @param timeToKillInSeconds the time to kill in seconds
     */
    public HardTimeoutExpirationPolicy(final long timeToKillInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
    }


    @Override
    public boolean isExpired(final TicketState ticketState) {
        return ticketState == null || ticketState.getCreationTime()
          .plus(this.timeToKillInSeconds, ChronoUnit.SECONDS).isBefore(ZonedDateTime.now(ZoneOffset.UTC));
    }

    @Override
    public Long getTimeToLive() {
        return this.timeToKillInSeconds;
    }

    @Override
    public Long getTimeToIdle() {
        return 0L;
    }
}
