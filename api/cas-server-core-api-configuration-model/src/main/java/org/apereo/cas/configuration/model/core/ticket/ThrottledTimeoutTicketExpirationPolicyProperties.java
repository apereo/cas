package org.apereo.cas.configuration.model.core.ticket;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link ThrottledTimeoutTicketExpirationPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Setter

@Accessors(chain = true)
public class ThrottledTimeoutTicketExpirationPolicyProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -2370751379747804646L;

    /**
     * Timeout in seconds to kill the session and consider tickets expired.
     */
    @DurationCapable
    private String timeToKillInSeconds;

    /**
     * Timeout in between each attempt.
     */
    @DurationCapable
    private String timeInBetweenUsesInSeconds;
}
