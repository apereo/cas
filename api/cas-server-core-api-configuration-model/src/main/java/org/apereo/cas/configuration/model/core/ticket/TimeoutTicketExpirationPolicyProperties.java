package org.apereo.cas.configuration.model.core.ticket;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("TimeoutTicketExpirationPolicyProperties")
public class TimeoutTicketExpirationPolicyProperties implements Serializable {

    private static final long serialVersionUID = 8635419913795245907L;

    /**
     * Maximum time in seconds. for TGTs to be live in CAS server.
     */
    private long maxTimeToLiveInSeconds;
}
