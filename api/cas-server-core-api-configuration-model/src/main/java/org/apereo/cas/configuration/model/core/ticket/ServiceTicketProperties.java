package org.apereo.cas.configuration.model.core.ticket;

import module java.base;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link ServiceTicketProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Getter
@Accessors(chain = true)
@Setter
public class ServiceTicketProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -7445209580598499921L;

    /**
     * Controls number of times a service ticket can be used within CAS server. Usage
     * in CAS context means service ticket validation
     * transaction.
     */
    private long numberOfUses = 1;

    /**
     * Time in seconds that service tickets should be considered live in CAS server.
     */
    @DurationCapable
    private String timeToKillInSeconds = "PT10S";

    /**
     * Maximum length of generated service tickets.
     */
    private int maxLength = 20;

    /**
     * Proxy granting ticket tracking policy for service tickets.
     */
    private TicketTrackingPolicyTypes proxyGrantingTicketTrackingPolicy = TicketTrackingPolicyTypes.ALL;
}
