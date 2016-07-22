package org.apereo.cas.configuration.model.core.ticket;

/**
 * This is {@link ServiceTicketProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class ServiceTicketProperties {
    private int numberOfUses = 1;
    private long timeToKillInSeconds = 10;
    private int maxLength = 20;

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(final int maxLength) {
        this.maxLength = maxLength;
    }
    
    public int getNumberOfUses() {
        return numberOfUses;
    }

    public void setNumberOfUses(final int numberOfUses) {
        this.numberOfUses = numberOfUses;
    }

    public long getTimeToKillInSeconds() {
        return timeToKillInSeconds;
    }

    public void setTimeToKillInSeconds(final long timeToKillInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
    }
}
