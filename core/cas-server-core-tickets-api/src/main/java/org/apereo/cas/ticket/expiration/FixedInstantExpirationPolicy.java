package org.apereo.cas.ticket.expiration;


import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;
import org.apereo.cas.util.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.io.Serial;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

/**
 * Ticket expiration policy based on a predefined fixed expiration date.
 *
 * @author Misagh
 * @since 7.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Slf4j
@AllArgsConstructor
@With
@Setter
@Getter
public class FixedInstantExpirationPolicy extends AbstractCasExpirationPolicy {
    @Serial
    private static final long serialVersionUID = 6728077010285422290L;

    @JsonProperty
    private Instant expirationInstant;

    @Override
    public boolean isExpired(final TicketGrantingTicketAwareTicket ticketState) {
        val expiringTime = toMaximumExpirationTime(ticketState);
        val now = ZonedDateTime.now(getClock());
        return expiringTime.isBefore(now);
    }

    @JsonIgnore
    @Override
    public Long getTimeToLive() {
        val currentTime = ZonedDateTime.now(getClock()).toInstant();
        return Duration.between(currentTime, expirationInstant).toSeconds();
    }
    
    @JsonIgnore
    @Override
    public ZonedDateTime toMaximumExpirationTime(final Ticket ticketState) {
        return DateTimeUtils.zonedDateTimeOf(expirationInstant);
    }
}
