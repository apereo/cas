package org.apereo.cas.configuration.model.core.ticket;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("HardTimeoutTicketExpirationPolicyProperties")
@Accessors(chain = true)
public class HardTimeoutTicketExpirationPolicyProperties implements Serializable {

    private static final long serialVersionUID = 4160963910346416908L;

    /**
     * Timeout in seconds to kill the session and consider tickets expired.
     */
    private long timeToKillInSeconds;
}
