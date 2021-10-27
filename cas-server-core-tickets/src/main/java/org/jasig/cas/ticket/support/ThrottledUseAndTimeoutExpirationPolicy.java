package org.jasig.cas.ticket.support;

import org.jasig.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Implementation of an expiration policy that adds the concept of saying that a
 * ticket can only be used once every X milliseconds to prevent misconfigured
 * clients from consuming resources by doing constant redirects.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Component("throttledUseAndTimeoutExpirationPolicy")
public final class ThrottledUseAndTimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = 205979491183779408L;

    /**
     * The Logger instance for this class. Using a transient instance field for the Logger doesn't work, on object
     * deserialization the field is null.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ThrottledUseAndTimeoutExpirationPolicy.class);

    /** The time to kill in milliseconds. */
    @Value("#{${tgt.throttled.maxTimeToLiveInSeconds:28800}*1000L}")
    private long timeToKillInMilliSeconds;

    /** Time time between which a ticket must wait to be used again. */
    @Value("#{${tgt.throttled.timeInBetweenUsesInSeconds:5}*1000L}")
    private long timeInBetweenUsesInMilliSeconds;

    /**
     * Instantiates a new Throttled use and timeout expiration policy.
     */
    public ThrottledUseAndTimeoutExpirationPolicy(){}

    public void setTimeInBetweenUsesInMilliSeconds(
        final long timeInBetweenUsesInMilliSeconds) {
        this.timeInBetweenUsesInMilliSeconds = timeInBetweenUsesInMilliSeconds;
    }

    public void setTimeToKillInMilliSeconds(final long timeToKillInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        final long currentTimeInMillis = System.currentTimeMillis();
        final long lastTimeTicketWasUsed = ticketState.getLastTimeUsed();

        if (ticketState.getCountOfUses() == 0
            && (currentTimeInMillis - lastTimeTicketWasUsed < this.timeToKillInMilliSeconds)) {
            LOGGER.debug("Ticket is not expired due to a count of zero and the time being less "
                    + "than the timeToKillInMilliseconds");
            return false;
        }

        if ((currentTimeInMillis - lastTimeTicketWasUsed >= this.timeToKillInMilliSeconds)) {
            LOGGER.debug("Ticket is expired due to the time being greater than the timeToKillInMilliseconds");
            return true;
        }

        if ((currentTimeInMillis - lastTimeTicketWasUsed <= this.timeInBetweenUsesInMilliSeconds)) {
            LOGGER.warn("Ticket is expired due to the time being less than the waiting period.");
            return true;
        }

        return false;
    }
}
