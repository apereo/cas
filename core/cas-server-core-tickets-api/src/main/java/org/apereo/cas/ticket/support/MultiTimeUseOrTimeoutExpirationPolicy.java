package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class MultiTimeUseOrTimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    private static final long serialVersionUID = -5704993954986738308L;

    /**
     * The Logger instance for this class. Using a transient instance field for the Logger doesn't work, on object
     * deserialization the field is null.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiTimeUseOrTimeoutExpirationPolicy.class);

    @JsonProperty("timeToLive")
    private long timeToKillInSeconds;

    @JsonProperty("numberOfUses")
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
     * @param numberOfUses        the number of uses
     * @param timeToKillInSeconds the time to kill in seconds
     */
    @JsonCreator
    public MultiTimeUseOrTimeoutExpirationPolicy(@JsonProperty("numberOfUses") final int numberOfUses,
                                                 @JsonProperty("timeToLive") final long timeToKillInSeconds) {
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
        final long countUses = ticketState.getCountOfUses();
        if (countUses >= this.numberOfUses) {
            LOGGER.debug("Ticket usage count [{}] is greater than or equal to [{}]. Ticket has expired", countUses, this.numberOfUses);
            return true;
        }

        final ZonedDateTime systemTime = getCurrentSystemTime();
        final ZonedDateTime lastTimeUsed = ticketState.getLastTimeUsed();
        final ZonedDateTime expirationTime = lastTimeUsed.plus(this.timeToKillInSeconds, ChronoUnit.SECONDS);

        if (systemTime.isAfter(expirationTime)) {
            LOGGER.debug("Ticket has expired because the difference between current time [{}] and ticket time [{}] is greater than or equal to [{}].",
                    systemTime, lastTimeUsed, this.timeToKillInSeconds);
            return true;
        }
        return false;
    }

    /**
     * Gets current system time.
     *
     * @return the current system time
     */
    protected ZonedDateTime getCurrentSystemTime() {
        return ZonedDateTime.now(ZoneOffset.UTC);
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

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final MultiTimeUseOrTimeoutExpirationPolicy rhs = (MultiTimeUseOrTimeoutExpirationPolicy) obj;
        return new EqualsBuilder()
                .append(this.timeToKillInSeconds, rhs.timeToKillInSeconds)
                .append(this.numberOfUses, rhs.numberOfUses)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(timeToKillInSeconds)
                .append(numberOfUses)
                .toHashCode();
    }

    /**
     * The Proxy ticket expiration policy.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    public static class ProxyTicketExpirationPolicy extends MultiTimeUseOrTimeoutExpirationPolicy {

        private static final long serialVersionUID = -5814201080268311070L;

        private ProxyTicketExpirationPolicy() {
        }

        /**
         * Instantiates a new proxy ticket expiration policy.
         *
         * @param numberOfUses        the number of uses
         * @param timeToKillInSeconds the time to kill in seconds
         */
        @JsonCreator
        public ProxyTicketExpirationPolicy(@JsonProperty("numberOfUses") final int numberOfUses,
                                           @JsonProperty("timeToKillInSeconds") final long timeToKillInSeconds) {
            super(numberOfUses, timeToKillInSeconds);
        }
    }

    /**
     * The Service ticket expiration policy.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    public static class ServiceTicketExpirationPolicy extends MultiTimeUseOrTimeoutExpirationPolicy {

        private static final long serialVersionUID = -5814201080268311070L;

        private ServiceTicketExpirationPolicy() {
        }

        /**
         * Instantiates a new Service ticket expiration policy.
         *
         * @param numberOfUses        the number of uses
         * @param timeToKillInSeconds the time to kill in seconds
         */
        @JsonCreator
        public ServiceTicketExpirationPolicy(@JsonProperty("numberOfUses") final int numberOfUses,
                                             @JsonProperty("timeToLive") final long timeToKillInSeconds) {
            super(numberOfUses, timeToKillInSeconds);
        }
    }
}
