package org.apereo.cas.services;

import org.apereo.cas.ticket.ExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Optional;

/**
 * This is {@link RegisteredServiceTicketGrantingTicketExpirationPolicy}.
 * This contract allows applications registered with CAS to define
 * an expiration policy for proxy granting tickets as to override
 * the default ticket granting ticket expiration policy.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceTicketGrantingTicketExpirationPolicy extends Serializable {
    /**
     * Get the TTL of this ticket, in seconds.
     *
     * @return the time to live
     */
    long getMaxTimeToLiveInSeconds();

    /**
     * To expiration policy.
     *
     * @return the expiration policy
     */
    @JsonIgnore
    Optional<ExpirationPolicy> toExpirationPolicy();
}
