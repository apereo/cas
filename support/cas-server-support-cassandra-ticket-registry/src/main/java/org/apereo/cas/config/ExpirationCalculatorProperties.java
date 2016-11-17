package org.apereo.cas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link ExpirationCalculatorProperties}.
 *
 * @author David Rodriguez
 * @since 5.0.0
 */
@ConfigurationProperties("cassandra")
public class ExpirationCalculatorProperties {

    private int maxTicketDuration;
    private long maxTimeToLiveInSeconds;
    private long timeToKillInSeconds;

    public int getMaxTicketDuration() {
        return maxTicketDuration;
    }

    public void setMaxTicketDuration(final int maxTicketDuration) {
        this.maxTicketDuration = maxTicketDuration;
    }

    public long getMaxTimeToLiveInSeconds() {
        return maxTimeToLiveInSeconds;
    }

    public void setMaxTimeToLiveInSeconds(final long maxTimeToLiveInSeconds) {
        this.maxTimeToLiveInSeconds = maxTimeToLiveInSeconds;
    }

    public long getTimeToKillInSeconds() {
        return timeToKillInSeconds;
    }

    public void setTimeToKillInSeconds(long timeToKillInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
    }
}



