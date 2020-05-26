package org.apereo.cas.ticket.expiration;

import org.apereo.cas.ticket.TicketState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.Assert;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * ExpirationPolicy that is based on certain number of uses of a ticket or a
 * certain time period for a ticket to exist.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MultiTimeUseOrTimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    private static final long serialVersionUID = -5704993954986738308L;

    @JsonProperty(value = "timeToLive")
    private long timeToKillInSeconds;

    @JsonProperty("numberOfUses")
    private long numberOfUses;

    /**
     * Instantiates a new multi time use or timeout expiration policy.
     *
     * @param numberOfUses        the number of uses
     * @param timeToKillInSeconds the time to kill in seconds
     */
    @JsonCreator
    public MultiTimeUseOrTimeoutExpirationPolicy(@JsonProperty("numberOfUses") final long numberOfUses, @JsonProperty("timeToLive") final long timeToKillInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
        this.numberOfUses = numberOfUses;
        Assert.isTrue(this.numberOfUses > 0, "numberOfUses must be greater than 0.");
        Assert.isTrue(this.timeToKillInSeconds > 0, "timeToKillInSeconds must be greater than 0.");
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        if (ticketState == null) {
            LOGGER.debug("Ticket state is null for [{}]. Ticket has expired.", this.getClass().getSimpleName());
            return true;
        }
        val countUses = ticketState.getCountOfUses();
        if (countUses >= this.numberOfUses) {
            LOGGER.debug("Ticket usage count [{}] is greater than or equal to [{}]. Ticket [{}] has expired",
                countUses, this.numberOfUses, ticketState.getId());
            return true;
        }
        val systemTime = ZonedDateTime.now(getClock());
        val lastTimeUsed = ticketState.getLastTimeUsed();
        val expirationTime = lastTimeUsed.plus(this.timeToKillInSeconds, ChronoUnit.SECONDS);
        if (systemTime.isAfter(expirationTime)) {
            LOGGER.debug("Ticket [{}] has expired because the difference between current time [{}] and ticket time [{}] is greater than or equal to [{}].",
                ticketState.getId(), systemTime, lastTimeUsed, this.timeToKillInSeconds);
            return true;
        }
        return super.isExpired(ticketState);
    }

    @Override
    public Long getTimeToLive() {
        return this.timeToKillInSeconds;
    }

    @JsonIgnore
    @Override
    public Long getTimeToIdle() {
        return 0L;
    }

    /**
     * The Proxy ticket expiration policy.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @ToString(callSuper = true)
    public static class ProxyTicketExpirationPolicy extends MultiTimeUseOrTimeoutExpirationPolicy {

        private static final long serialVersionUID = -5814201080268311070L;

        /**
         * Instantiates a new proxy ticket expiration policy.
         *
         * @param numberOfUses        the number of uses
         * @param timeToKillInSeconds the time to kill in seconds
         */
        @JsonCreator
        public ProxyTicketExpirationPolicy(@JsonProperty("numberOfUses") final long numberOfUses, @JsonProperty("timeToLive") final long timeToKillInSeconds) {
            super(numberOfUses, timeToKillInSeconds);
        }
    }

    /**
     * The Service ticket expiration policy.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @ToString(callSuper = true)
    public static class ServiceTicketExpirationPolicy extends MultiTimeUseOrTimeoutExpirationPolicy {

        private static final long serialVersionUID = -5814201080268311070L;

        /**
         * Instantiates a new Service ticket expiration policy.
         *
         * @param numberOfUses        the number of uses
         * @param timeToKillInSeconds the time to kill in seconds
         */
        @JsonCreator
        public ServiceTicketExpirationPolicy(@JsonProperty("numberOfUses") final long numberOfUses, @JsonProperty("timeToLive") final long timeToKillInSeconds) {
            super(numberOfUses, timeToKillInSeconds);
        }
    }
}
