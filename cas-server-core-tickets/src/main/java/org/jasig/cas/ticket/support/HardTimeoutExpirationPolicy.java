package org.jasig.cas.ticket.support;

import org.jasig.cas.ticket.TicketState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Ticket expiration policy based on a hard timeout from ticket creation time rather than the
 * "idle" timeout provided by {@link TimeoutExpirationPolicy}.
 *
 * @author Andrew Feller
 * @since 3.1.2
 */
@Component("hardTimeoutExpirationPolicy")
public final class HardTimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = 6728077010285422290L;

    /** The time to kill in milliseconds. */
    @Value("#{${tgt.timeout.hard.maxTimeToLiveInSeconds:28800}*1000L}")
    private long timeToKillInMilliSeconds;

    /** No-arg constructor for serialization support. */
    private HardTimeoutExpirationPolicy() {}


    /**
     * Instantiates a new hard timeout expiration policy.
     *
     * @param timeToKillInMilliSeconds the time to kill in milli seconds
     */
    public HardTimeoutExpirationPolicy(final long timeToKillInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
    }

    /**
     * Instantiates a new Hard timeout expiration policy.
     *
     * @param timeToKill the time to kill
     * @param timeUnit the time unit
     */
    public HardTimeoutExpirationPolicy(final long timeToKill, final TimeUnit timeUnit) {
        this.timeToKillInMilliSeconds = timeUnit.toMillis(timeToKill);
    }


    /**
     * Init .
     */
    @PostConstruct
    public void init() {
        this.timeToKillInMilliSeconds = TimeUnit.SECONDS.toMillis(this.timeToKillInMilliSeconds);
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        return (ticketState == null)
                || (System.currentTimeMillis() - ticketState.getCreationTime() >= this.timeToKillInMilliSeconds);
    }
}
