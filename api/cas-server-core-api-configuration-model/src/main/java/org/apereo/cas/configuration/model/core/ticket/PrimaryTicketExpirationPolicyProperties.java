package org.apereo.cas.configuration.model.core.ticket;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("PrimaryTicketExpirationPolicyProperties")
public class PrimaryTicketExpirationPolicyProperties implements Serializable {

    private static final long serialVersionUID = 3345179252583399336L;

    /**
     * Maximum time in seconds tickets would be live in CAS server.
     */
    private long maxTimeToLiveInSeconds = 28_800;

    /**
     * Time in seconds after which tickets would be destroyed after a period of inactivity.
     */
    private long timeToKillInSeconds = 7_200;

}
