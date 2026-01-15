package org.apereo.cas.configuration.model.core.ticket;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link TransientSessionTicketProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class TransientSessionTicketProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -3690545027059561010L;

    /**
     * Controls number of times a ticket can be used within CAS server.
     */
    private long numberOfUses = 1;

    /**
     * Number of seconds after which this ticket becomes invalid.
     */
    private long timeToKillInSeconds = TimeUnit.MINUTES.toSeconds(15);
}
