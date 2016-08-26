package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.support.oauth.util.OAuthTokenExporationPolicy;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link OAuthAccessTokenExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuthAccessTokenExpirationPolicy extends OAuthTokenExporationPolicy {
    private static final long serialVersionUID = -8383186650682727360L;

    /**
     * Instantiates a new OAuth access token expiration policy.
     *  @param timeToKill    the time to kill
     * @param maxTimeToLive the max time to live
     * @param timeUnit      the time unit
     */
    public OAuthAccessTokenExpirationPolicy(final long timeToKill, final long maxTimeToLive, final TimeUnit timeUnit) {
        super(-1, timeToKill, maxTimeToLive, timeUnit);
    }
}
