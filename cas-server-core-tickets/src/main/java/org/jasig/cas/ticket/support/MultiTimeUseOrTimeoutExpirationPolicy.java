package org.jasig.cas.ticket.support;

import org.jasig.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * ExpirationPolicy that is based on certain number of uses of a ticket or a
 * certain time period for a ticket to exist.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Component("multiTimeUseOrTimeoutExpirationPolicy")
public final class MultiTimeUseOrTimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = -5704993954986738308L;

    /**
     * The Logger instance for this class. Using a transient instance field for the Logger doesn't work, on object
     * deserialization the field is null.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiTimeUseOrTimeoutExpirationPolicy.class);

    /** The time to kill in milliseconds. */
    @Value("#{${st.timeToKillInSeconds:10}*1000L}")
    private final long timeToKillInMilliSeconds;

    /** The maximum number of uses before expiration. */

    @Value("${st.numberOfUses:1}")
    private final int numberOfUses;


    /** No-arg constructor for serialization support. */
    private MultiTimeUseOrTimeoutExpirationPolicy() {
        this.timeToKillInMilliSeconds = 0;
        this.numberOfUses = 0;
    }


    /**
     * Instantiates a new multi time use or timeout expiration policy.
     *
     * @param numberOfUses the number of uses
     * @param timeToKillInMilliSeconds the time to kill in milli seconds
     */
    public MultiTimeUseOrTimeoutExpirationPolicy(final int numberOfUses,
        final long timeToKillInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
        this.numberOfUses = numberOfUses;
        Assert.isTrue(this.numberOfUses > 0, "numberOfUses must be greater than 0.");
        Assert.isTrue(this.timeToKillInMilliSeconds > 0, "timeToKillInMilliseconds must be greater than 0.");

    }

    /**
     * Instantiates a new multi time use or timeout expiration policy.
     *
     * @param numberOfUses the number of uses
     * @param timeToKill the time to kill
     * @param timeUnit the time unit
     */
    public MultiTimeUseOrTimeoutExpirationPolicy(final int numberOfUses, final long timeToKill,
            final TimeUnit timeUnit) {
        this(numberOfUses, timeUnit.toMillis(timeToKill));
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        if (ticketState == null) {
            LOGGER.debug("Ticket state is null for {}", this.getClass().getSimpleName());
            return true;
        }
        final long countUses = ticketState.getCountOfUses();
        if (countUses >= this.numberOfUses) {
            LOGGER.debug("Ticket usage count {} is greater than or equal to {}", countUses, this.numberOfUses);
            return true;
        }

        final long systemTime = System.currentTimeMillis();
        final long lastTimeUsed = ticketState.getLastTimeUsed();
        final long difference = systemTime - lastTimeUsed;

        if (difference >= this.timeToKillInMilliSeconds) {
            LOGGER.debug("Ticket has expired because the difference between current time [{}] "
                + "and ticket time [{}] is greater than or equal to [{}]", systemTime, lastTimeUsed,
                this.timeToKillInMilliSeconds);
            return true;
        }
        return false;
    }
}
