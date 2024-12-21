package org.apereo.cas.ticket.expiration;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.Assert;
import java.io.Serial;
import java.time.ZonedDateTime;

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
    @Serial
    private static final long serialVersionUID = -5704993954986738308L;

    @JsonProperty("timeToLive")
    private long timeToKillInSeconds;

    @JsonProperty("numberOfUses")
    private long numberOfUses;

    @JsonCreator
    public MultiTimeUseOrTimeoutExpirationPolicy(@JsonProperty("numberOfUses") final long numberOfUses,
                                                 @JsonProperty("timeToLive") final long timeToKillInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
        this.numberOfUses = numberOfUses;
        Assert.isTrue(numberOfUses > 0, "numberOfUses must be greater than 0.");
        Assert.isTrue(timeToKillInSeconds > 0, "timeToKillInSeconds must be greater than 0.");
    }

    @Override
    public ZonedDateTime toMaximumExpirationTime(final Ticket ticketState) {
        val creationTime = ticketState.getCreationTime();
        return creationTime.plusSeconds(this.timeToKillInSeconds);
    }

    @Override
    public boolean isExpired(final TicketGrantingTicketAwareTicket ticketState) {
        if (ticketState == null) {
            LOGGER.debug("Ticket state is null for [{}]. Ticket has expired.", getClass().getSimpleName());
            return true;
        }
        val countUses = ticketState.getCountOfUses();
        if (countUses >= numberOfUses) {
            LOGGER.debug("Ticket usage count [{}] is greater than or equal to [{}]. Ticket [{}] has expired",
                countUses, numberOfUses, ticketState.getId());
            return true;
        }
        val systemTime = ZonedDateTime.now(getClock());
        val creationTime = ticketState.getCreationTime();
        val expiringTime = creationTime.plusSeconds(this.timeToKillInSeconds);
        if (expiringTime.isBefore(systemTime)) {
            LOGGER.debug("Ticket [{}] has expired; difference between current time [{}] and ticket creation time [{}] is greater than or equal to [{}].",
                ticketState.getId(), systemTime, creationTime, this.timeToKillInSeconds);
            return true;
        }
        return super.isExpired(ticketState);
    }

    @Override
    public Long getTimeToLive() {
        return timeToKillInSeconds;
    }
    
    /**
     * The Proxy ticket expiration policy.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @ToString(callSuper = true)
    public static class ProxyTicketExpirationPolicy extends MultiTimeUseOrTimeoutExpirationPolicy {

        @Serial
        private static final long serialVersionUID = -5814201080268311070L;

        /**
         * Instantiates a new proxy ticket expiration policy.
         *
         * @param numberOfUses        the number of uses
         * @param timeToKillInSeconds the time to kill in seconds
         */
        @JsonCreator
        public ProxyTicketExpirationPolicy(@JsonProperty("numberOfUses") final long numberOfUses,
                                           @JsonProperty("timeToLive") final long timeToKillInSeconds) {
            super(numberOfUses, timeToKillInSeconds);
        }
    }

    /**
     * The Service ticket expiration policy.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @ToString(callSuper = true)
    public static class ServiceTicketExpirationPolicy extends MultiTimeUseOrTimeoutExpirationPolicy {

        @Serial
        private static final long serialVersionUID = -5814201080268311070L;

        @JsonCreator
        public ServiceTicketExpirationPolicy(@JsonProperty("numberOfUses") final long numberOfUses,
                                             @JsonProperty("timeToLive") final long timeToKillInSeconds) {
            super(numberOfUses, timeToKillInSeconds);
        }
    }

    /**
     * The Service ticket expiration policy.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @ToString(callSuper = true)
    public static class TransientSessionTicketExpirationPolicy extends MultiTimeUseOrTimeoutExpirationPolicy {

        @Serial
        private static final long serialVersionUID = -5814201080268311070L;

        @JsonCreator
        public TransientSessionTicketExpirationPolicy(@JsonProperty("numberOfUses") final long numberOfUses,
                                                      @JsonProperty("timeToLive") final long timeToKillInSeconds) {
            super(numberOfUses, timeToKillInSeconds);
        }
    }
}
