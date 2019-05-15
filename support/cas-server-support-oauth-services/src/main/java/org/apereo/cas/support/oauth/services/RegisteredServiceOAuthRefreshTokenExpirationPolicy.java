package org.apereo.cas.support.oauth.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link RegisteredServiceOAuthRefreshTokenExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceOAuthRefreshTokenExpirationPolicy extends Serializable {
    /**
     * Time to kill for this ticket.
     * @return time to kill.
     */
    String getTimeToKill();
}
