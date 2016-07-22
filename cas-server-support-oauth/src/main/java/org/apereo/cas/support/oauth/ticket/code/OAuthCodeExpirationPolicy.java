package org.apereo.cas.support.oauth.ticket.code;

import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link OAuthCodeExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuthCodeExpirationPolicy extends MultiTimeUseOrTimeoutExpirationPolicy {
    private static final long serialVersionUID = -8383186621682727360L;

    /**
     * Instantiates a new O auth code expiration policy.
     *
     * @param numberOfUses             the number of uses
     * @param timeToKillInMilliSeconds the time to kill in milli seconds
     */
    public OAuthCodeExpirationPolicy(final int numberOfUses,
                                     final long timeToKillInMilliSeconds) {
        super(numberOfUses, timeToKillInMilliSeconds);
    }

    /**
     * Instantiates a new O auth code expiration policy.
     *
     * @param numberOfUses the number of uses
     * @param timeToKill   the time to kill
     * @param timeUnit     the time unit
     */
    public OAuthCodeExpirationPolicy(final int numberOfUses, final long timeToKill, final TimeUnit timeUnit) {
        super(numberOfUses, timeToKill, timeUnit);
    }
}
