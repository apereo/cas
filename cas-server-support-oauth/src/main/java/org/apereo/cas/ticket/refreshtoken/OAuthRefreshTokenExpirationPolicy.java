package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.support.oauth.util.OAuthTokenExporationPolicy;

import java.util.concurrent.TimeUnit;

/**
 * This is OAuth refresh token expiration policy (max time to live = 1 month by default).
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public class OAuthRefreshTokenExpirationPolicy extends OAuthTokenExporationPolicy {

    public OAuthRefreshTokenExpirationPolicy(final int numberOfUses, final long timeToKill, final long maxTimeToLive, final TimeUnit timeUnit) {
        super(numberOfUses, timeToKill, maxTimeToLive, timeUnit);
    }
}
