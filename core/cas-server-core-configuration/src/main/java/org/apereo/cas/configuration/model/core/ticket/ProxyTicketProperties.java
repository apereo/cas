package org.apereo.cas.configuration.model.core.ticket;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link ProxyTicketProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
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
    
    public int getNumberOfUses() {
        return numberOfUses;
    }

    public void setNumberOfUses(final int numberOfUses) {
        this.numberOfUses = numberOfUses;
    }

    public int getTimeToKillInSeconds() {
        return timeToKillInSeconds;
    }

    public void setTimeToKillInSeconds(final int timeToKillInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
    }
}
