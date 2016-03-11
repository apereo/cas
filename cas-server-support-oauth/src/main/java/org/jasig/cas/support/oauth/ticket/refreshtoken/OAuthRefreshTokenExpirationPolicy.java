package org.jasig.cas.support.oauth.ticket.refreshtoken;

import org.jasig.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This is OAuth refresh token expiration policy (max time to live = 1 month by default).
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
@Component("oAuthRefreshTokenExpirationPolicy")
public class OAuthRefreshTokenExpirationPolicy extends HardTimeoutExpirationPolicy {

    /**
     * Build an OAuth refresh token expiration policy.
     *
     * @param timeToKillInMilliSeconds time to kill in seconds
     */
    @Autowired
    public OAuthRefreshTokenExpirationPolicy(@Value("#{${oauth.refresh.token.timeToKillInSeconds:2592000}*1000L}")
                                             final long timeToKillInMilliSeconds) {
        super(timeToKillInMilliSeconds);
    }
}
