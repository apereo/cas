package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link RegisteredServiceProxyGrantingTicketExpirationPolicy}.
 * This contract allows applications registered with CAS to define
 * an expiration policy for proxy granting tickets as to override
 * the default ticket granting ticket expiration policy.
 *
 * @author Jerome LELEU
 * @since 6.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceProxyGrantingTicketExpirationPolicy extends Serializable {
    /**
     * Get the TTL of this ticket, in seconds.
     *
     * @return the time to live
     */
    long getMaxTimeToLiveInSeconds();
}
