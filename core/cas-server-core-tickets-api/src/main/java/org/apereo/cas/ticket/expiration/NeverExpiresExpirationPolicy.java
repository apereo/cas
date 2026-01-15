package org.apereo.cas.ticket.expiration;

import module java.base;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * {@link NeverExpiresExpirationPolicy} always answers false when asked if a Ticket is
 * expired. Use this policy when you want a Ticket to live forever, or at least
 * as long as the particular CAS Universe exists.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NeverExpiresExpirationPolicy extends AbstractCasExpirationPolicy {

    /**
     * Static instance of the policy.
     */
    public static final ExpirationPolicy INSTANCE = new NeverExpiresExpirationPolicy();

    private static final long MAX_EXPIRATION_IN_YEARS = 50L;

    @Serial
    private static final long serialVersionUID = 3833747698242303540L;

    @Override
    public boolean isExpired(final TicketGrantingTicketAwareTicket ticketState) {
        return false;
    }

    @JsonIgnore
    @Override
    public Long getTimeToLive() {
        return (long) Integer.MAX_VALUE;
    }
    
    @Override
    public ZonedDateTime toMaximumExpirationTime(final Ticket ticketState) {
        return ZonedDateTime.now(Clock.systemUTC()).plusYears(MAX_EXPIRATION_IN_YEARS);
    }

}
