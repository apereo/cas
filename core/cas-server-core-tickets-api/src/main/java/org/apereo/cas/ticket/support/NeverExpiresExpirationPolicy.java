package org.apereo.cas.ticket.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.ticket.TicketState;

/**
 * NeverExpiresExpirationPolicy always answers false when asked if a Ticket is
 * expired. Use this policy when you want a Ticket to live forever, or at least
 * as long as the particular CAS Universe exists.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY)
public class NeverExpiresExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serializable Unique ID. */
    private static final long serialVersionUID = 3833747698242303540L;

    /**
     * Instantiates a new Never expires expiration policy.
     */
    public NeverExpiresExpirationPolicy() {}

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
        return new EqualsBuilder()
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .toHashCode();
    }
}
