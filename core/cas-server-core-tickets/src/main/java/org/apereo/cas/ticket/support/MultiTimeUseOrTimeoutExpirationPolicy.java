package org.apereo.cas.ticket.support;

import org.apereo.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * ExpirationPolicy that is based on certain number of uses of a ticket or a
 * certain time period for a ticket to exist.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class MultiTimeUseOrTimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    private static final long serialVersionUID = -5704993954986738308L;

    /**
     * The Logger instance for this class. Using a transient instance field for the Logger doesn't work, on object
     * deserialization the field is null.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiTimeUseOrTimeoutExpirationPolicy.class);

    private long timeToKillInSeconds;
    private int numberOfUses;

    /**
     * No-arg constructor for serialization support.
     */
    private MultiTimeUseOrTimeoutExpirationPolicy() {
        this.timeToKillInSeconds = 0;
        this.numberOfUses = 0;
    }


    /**
     * Instantiates a new multi time use or timeout expiration policy.
     *
     * @param numberOfUses             the number of uses
     * @param timeToKillInSeconds the time to kill in seconds
     */
    public MultiTimeUseOrTimeoutExpirationPolicy(final int numberOfUses,
                                                 final long timeToKillInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
        this.numberOfUses = numberOfUses;
        Assert.isTrue(this.numberOfUses > 0, "numberOfUses must be greater than 0.");
        Assert.isTrue(this.timeToKillInSeconds > 0, "timeToKillInSeconds must be greater than 0.");

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

        final ZonedDateTime systemTime = ZonedDateTime.now(ZoneOffset.UTC);
        final ZonedDateTime lastTimeUsed = ticketState.getLastTimeUsed();
        final ZonedDateTime expirationTime = lastTimeUsed.plus(this.timeToKillInSeconds, ChronoUnit.SECONDS);

        if (systemTime.isAfter(expirationTime)) {
            LOGGER.debug("Ticket has expired because the difference between current time [{}] "
                            + "and ticket time [{}] is greater than or equal to [{}]", systemTime, lastTimeUsed,
                    this.timeToKillInSeconds);
            return true;
        }
        return false;
    }

    @Override
    public Long getTimeToLive() {
        return this.timeToKillInSeconds;
    }

    @Override
    public Long getTimeToIdle() {
        return 0L;
    }

    /**
     * The Proxy ticket expiration policy.
     */
    public static class ProxyTicketExpirationPolicy extends MultiTimeUseOrTimeoutExpirationPolicy {

        private static final long serialVersionUID = -5814201080268311070L;

        /**
         * Instantiates a new proxy ticket expiration policy.
         *
         * @param numberOfUses        the number of uses
         * @param timeToKillInSeconds the time to kill in seconds
         */
        public ProxyTicketExpirationPolicy(final int numberOfUses,
                                           final long timeToKillInSeconds) {
            super(numberOfUses, timeToKillInSeconds);
        }
    }

    /**
     * The Service ticket expiration policy.
     */
    public static class ServiceTicketExpirationPolicy extends MultiTimeUseOrTimeoutExpirationPolicy {

        private static final long serialVersionUID = -5814201080268311070L;

        /**
         * Instantiates a new Service ticket expiration policy.
         *
         * @param numberOfUses        the number of uses
         * @param timeToKillInSeconds the time to kill in seconds
         */
        public ServiceTicketExpirationPolicy(final int numberOfUses,
                                             final long timeToKillInSeconds) {
            super(numberOfUses, timeToKillInSeconds);
        }
    }
}
