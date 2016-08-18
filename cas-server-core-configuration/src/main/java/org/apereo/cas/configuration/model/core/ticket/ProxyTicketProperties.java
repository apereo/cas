package org.apereo.cas.configuration.model.core.ticket;

/**
 * This is {@link ProxyTicketProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class ProxyTicketProperties {
    private int numberOfUses = 1;
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
