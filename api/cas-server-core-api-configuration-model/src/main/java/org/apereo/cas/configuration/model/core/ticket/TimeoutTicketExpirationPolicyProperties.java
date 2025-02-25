package org.apereo.cas.configuration.model.core.ticket;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link TimeoutTicketExpirationPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Setter
@Accessors(chain = true)
public class TimeoutTicketExpirationPolicyProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 8635419913795245907L;

    /**
     * Maximum time in seconds. for TGTs to be live in CAS server.
     */
    @DurationCapable
    private String maxTimeToLiveInSeconds;
}
