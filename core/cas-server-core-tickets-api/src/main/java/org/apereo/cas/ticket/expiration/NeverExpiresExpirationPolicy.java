package org.apereo.cas.ticket.expiration;

import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketState;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * NeverExpiresExpirationPolicy always answers false when asked if a Ticket is
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

    /**
     * Serializable Unique ID.
     */
    private static final long serialVersionUID = 3833747698242303540L;

    @Override
    public boolean isExpired(final TicketState ticketState) {
        return false;
    }

    @JsonIgnore
    @Override
    public Long getTimeToLive() {
        return (long) Integer.MAX_VALUE;
    }

    @JsonIgnore
    @Override
    public Long getTimeToIdle() {
        return (long) Integer.MAX_VALUE;
    }

}
