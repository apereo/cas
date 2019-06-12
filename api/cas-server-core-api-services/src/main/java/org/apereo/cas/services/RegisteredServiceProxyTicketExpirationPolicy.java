package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link RegisteredServiceProxyTicketExpirationPolicy}.
 * This contract allows applications registered with CAS to define
 * an expiration policy for proxy tickets as to override
 * the default timeouts and settings applied globally.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceProxyTicketExpirationPolicy extends Serializable {
    /**
     * Gets number of times this ticket can be used.
     *
     * @return the number of uses
     */
    long getNumberOfUses();

    /**
     * Get the TTL of this ticket, in seconds.
     *
     * @return the time to live
     */
    String getTimeToLive();
}
