package org.apereo.cas.support.oauth.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link RegisteredServiceOAuthCodeExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceOAuthCodeExpirationPolicy extends Serializable {
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
