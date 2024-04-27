package org.apereo.cas.ticket.expiration;

import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.ZonedDateTime;

/**
 * {@link AlwaysExpiresExpirationPolicy} always answers true when asked if a Ticket is
 * expired.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AlwaysExpiresExpirationPolicy extends AbstractCasExpirationPolicy {

    /**
     * Static instance of the policy.
     */
    public static final ExpirationPolicy INSTANCE = new AlwaysExpiresExpirationPolicy();

    @Serial
    private static final long serialVersionUID = 3836547698242303540L;

    @Override
    public boolean isExpired(final TicketGrantingTicketAwareTicket ticketState) {
        return true;
    }

    @JsonIgnore
    @Override
    public Long getTimeToLive() {
        return 0L;
    }
    
    @JsonIgnore
    @Override
    public ZonedDateTime toMaximumExpirationTime(final Ticket ticketState) {
        return ZonedDateTime.now(getClock());
    }
}
