package org.jasig.cas.support.oauth.ticket.code;

import org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link OAuthCodeExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("oAuthCodeExpirationPolicy")
public class OAuthCodeExpirationPolicy extends MultiTimeUseOrTimeoutExpirationPolicy {
    private static final long serialVersionUID = -8383186621682727360L;

    /**
     * Instantiates a new O auth code expiration policy.
     *
     * @param numberOfUses             the number of uses
     * @param timeToKillInMilliSeconds the time to kill in milli seconds
     */
    @Autowired
    public OAuthCodeExpirationPolicy(@Value("${oauth.code.numberOfUses:1}")
                                     final int numberOfUses,
                                     @Value("#{${oauth.code.timeToKillInSeconds:10}*1000}")
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
