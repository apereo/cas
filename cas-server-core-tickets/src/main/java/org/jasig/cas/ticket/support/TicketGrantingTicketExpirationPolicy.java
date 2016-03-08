package org.jasig.cas.ticket.support;

import org.jasig.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * Provides the Ticket Granting Ticket expiration policy.  Ticket Granting Tickets
 * can be used any number of times, have a fixed lifetime, and an idle timeout.
 *
 * @author William G. Thompson, Jr.
 * @since 3.4.10
 */
@Component("ticketGrantingTicketExpirationPolicy")
public final class TicketGrantingTicketExpirationPolicy extends AbstractCasExpirationPolicy implements InitializingBean {

    /** Serialization support. */
    private static final long serialVersionUID = 7670537200691354820L;

    /**
     * The Logger instance for this class. Using a transient instance field for the Logger doesn't work, on object
     * deserialization the field is null.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketGrantingTicketExpirationPolicy.class);

    /** Maximum time this ticket is valid.  */
    @Value("#{${tgt.maxTimeToLiveInSeconds:28800}*1000L}")
    private long maxTimeToLiveInMilliSeconds;

    /** Time to kill in milliseconds. */
    @Value("#{${tgt.timeToKillInSeconds:7200}*1000L}")
    private long timeToKillInMilliSeconds;

    private TicketGrantingTicketExpirationPolicy() {}

    /**
     * Instantiates a new Ticket granting ticket expiration policy.
     *
     * @param maxTimeToLive the max time to live
     * @param timeToKill the time to kill
     * @param timeUnit the time unit
     */
    public TicketGrantingTicketExpirationPolicy(final long maxTimeToLive, final long timeToKill, final TimeUnit timeUnit) {
        this.maxTimeToLiveInMilliSeconds = timeUnit.toMillis(maxTimeToLive);
        this.timeToKillInMilliSeconds = timeUnit.toMillis(timeToKill);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.isTrue((maxTimeToLiveInMilliSeconds >= timeToKillInMilliSeconds),
                "maxTimeToLiveInMilliSeconds must be greater than or equal to timeToKillInMilliSeconds.");
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        final long currentSystemTimeInMillis = System.currentTimeMillis();

        // Ticket has been used, check maxTimeToLive (hard window)
        if ((currentSystemTimeInMillis - ticketState.getCreationTime() >= maxTimeToLiveInMilliSeconds)) {
            LOGGER.debug("Ticket is expired because the time since creation is greater than maxTimeToLiveInMilliSeconds");
            return true;
        }

        // Ticket is within hard window, check timeToKill (sliding window)
        if ((currentSystemTimeInMillis - ticketState.getLastTimeUsed() >= timeToKillInMilliSeconds)) {
            LOGGER.debug("Ticket is expired because the time since last use is greater than timeToKillInMilliseconds");
            return true;
        }

        return false;
    }

}
