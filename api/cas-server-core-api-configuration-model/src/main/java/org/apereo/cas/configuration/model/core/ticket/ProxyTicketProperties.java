package org.apereo.cas.configuration.model.core.ticket;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link ProxyTicketProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class ProxyTicketProperties implements Serializable {

    private static final long serialVersionUID = -3690545027059561010L;

    /**
     * Number of uses allowed.
     */
    private int numberOfUses = 1;

    /**
     * Number of seconds after which this ticket becomes invalid.
     */
    private int timeToKillInSeconds = 10;
}
