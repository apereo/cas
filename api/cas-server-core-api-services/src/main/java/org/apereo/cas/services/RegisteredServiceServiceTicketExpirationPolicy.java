package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link RegisteredServiceServiceTicketExpirationPolicy}.
 * This contract allows applications registered with CAS to define
 * an expiration policy for service tickets as to override
 * the default timeouts and settings applied globally.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceServiceTicketExpirationPolicy extends Serializable {
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

    /**
     * Undefined registered service service ticket expiration policy.
     *
     * @return the registered service service ticket expiration policy
     */
    static RegisteredServiceServiceTicketExpirationPolicy undefined() {
        return new RegisteredServiceServiceTicketExpirationPolicy() {
            private static final long serialVersionUID = -6204076273553630977L;

            @Override
            public long getNumberOfUses() {
                return Long.MIN_VALUE;
            }

            @Override
            public String getTimeToLive() {
                return null;
            }
        };
    }
}
