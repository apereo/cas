package org.apereo.cas.configuration.model.core.ticket;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link PrimaryTicketExpirationPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Getter
@Setter
@Accessors(chain = true)

public class PrimaryTicketExpirationPolicyProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 3345179252583399336L;

    /**
     * Maximum time in seconds tickets would be live in CAS server.
     */
    @DurationCapable
    private String maxTimeToLiveInSeconds = "PT8H";

    /**
     * Time in seconds after which tickets would be
     * destroyed after a period of inactivity.
     */
    @DurationCapable
    private String timeToKillInSeconds = "PT2H";

}
