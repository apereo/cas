package org.apereo.cas.configuration.model.core.ticket;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link HardTimeoutTicketExpirationPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Setter

@Accessors(chain = true)
public class HardTimeoutTicketExpirationPolicyProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 4160963910346416908L;

    /**
     * Timeout in seconds to kill the session and consider tickets expired.
     */
    @DurationCapable
    private String timeToKillInSeconds;
}
