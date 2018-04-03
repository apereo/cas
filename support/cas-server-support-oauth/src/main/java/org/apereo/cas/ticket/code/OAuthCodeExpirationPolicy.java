package org.apereo.cas.ticket.code;

import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;

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
     * @param timeToKillInSeconds the time to kill in seconds
     */
    public OAuthCodeExpirationPolicy(final int numberOfUses,
                                     final long timeToKillInSeconds) {
        super(numberOfUses, timeToKillInSeconds);
    }
}
