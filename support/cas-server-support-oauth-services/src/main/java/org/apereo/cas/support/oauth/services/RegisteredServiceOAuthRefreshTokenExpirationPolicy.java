package org.apereo.cas.support.oauth.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/**
 * This is {@link RegisteredServiceOAuthRefreshTokenExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceOAuthRefreshTokenExpirationPolicy extends Serializable {
    /**
     * Time to kill for this ticket.
     *
     * @return time to kill.
     */
    String getTimeToKill();

    /**
     * Maximum number of active refresh tokens that an application
     * can receive. If the application requests more that this limit,
     * the request will be denied and the refresh token will not be issued.
     *
     * @return number of tokens
     */
    long getMaxActiveTokens();
}
