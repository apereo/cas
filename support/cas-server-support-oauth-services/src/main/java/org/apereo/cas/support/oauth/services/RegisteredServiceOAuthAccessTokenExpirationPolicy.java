package org.apereo.cas.support.oauth.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/**
 * This is {@link RegisteredServiceOAuthAccessTokenExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceOAuthAccessTokenExpirationPolicy extends Serializable {

    /**
     * Maximum time this token is valid.
     *
     * @return max time to kill.
     */
    String getMaxTimeToLive();

    /**
     * Get the TTL of this ticket, in seconds.
     *
     * @return the time to live
     */
    String getTimeToKill();

    /**
     * Maximum number of active access tokens that an application
     * can receive. If the application requests more that this limit,
     * the request will be denied and the access token will not be issued.
     *
     * @return number of tokens
     */
    long getMaxActiveTokens();
}
