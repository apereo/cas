package org.apereo.cas.ticket.code;

import org.apereo.cas.support.oauth.util.OAuthTokenExporationPolicy;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link OAuthCodeExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuthCodeExpirationPolicy extends OAuthTokenExporationPolicy {
    private static final long serialVersionUID = -8383186621682727360L;

    /**
     * Instantiates a new O auth code expiration policy.
     *
     * @param numberOfUses  the number of uses
     * @param maxTimeToLive the time to kill
     * @param timeUnit      the time unit
     */
    public OAuthCodeExpirationPolicy(int numberOfUses, long maxTimeToLive, TimeUnit timeUnit) {
        super(numberOfUses, -1, maxTimeToLive, timeUnit);
    }
}
