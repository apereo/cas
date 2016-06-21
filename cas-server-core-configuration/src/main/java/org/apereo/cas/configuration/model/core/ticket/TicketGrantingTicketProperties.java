package org.apereo.cas.configuration.model.core.ticket;

/**
 * This is {@link TicketGrantingTicketProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class TicketGrantingTicketProperties {
    private int maxLength = 50;
    private int maxTimeToLiveInSeconds = 28800;
    private int timeToKillInSeconds = 7200;
    private boolean onlyTrackMostRecentSession = true;

    private HardTimeout hardTimeout = new HardTimeout();
    private ThrottledTimeout throttledTimeout = new ThrottledTimeout();
    private Timeout timeout = new Timeout();

    public Timeout getTimeout() {
        return timeout;
    }

    public void setTimeout(final Timeout timeout) {
        this.timeout = timeout;
    }

    public ThrottledTimeout getThrottledTimeout() {
        return throttledTimeout;
    }

    public void setThrottledTimeout(final ThrottledTimeout throttledTimeout) {
        this.throttledTimeout = throttledTimeout;
    }

    public HardTimeout getHardTimeout() {
        return hardTimeout;
    }

    public void setHardTimeout(final HardTimeout hardTimeout) {
        this.hardTimeout = hardTimeout;
    }

    public boolean isOnlyTrackMostRecentSession() {
        return onlyTrackMostRecentSession;
    }

    public int getTimeToKillInSeconds() {
        return timeToKillInSeconds;
    }

    public void setTimeToKillInSeconds(final int timeToKillInSeconds) {
        this.timeToKillInSeconds = timeToKillInSeconds;
    }

    public void setOnlyTrackMostRecentSession(final boolean onlyTrackMostRecentSession) {
        this.onlyTrackMostRecentSession = onlyTrackMostRecentSession;
    }

    public int getMaxTimeToLiveInSeconds() {
        return maxTimeToLiveInSeconds;
    }

    public void setMaxTimeToLiveInSeconds(final int maxTimeToLiveInSeconds) {
        this.maxTimeToLiveInSeconds = maxTimeToLiveInSeconds;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(final int maxLength) {
        this.maxLength = maxLength;
    }

    public static class HardTimeout {
        private long timeToKillInSeconds;

        public long getTimeToKillInSeconds() {
            return timeToKillInSeconds;
        }

        public void setTimeToKillInSeconds(final long timeToKillInSeconds) {
            this.timeToKillInSeconds = timeToKillInSeconds;
        }
    }

    public static class Timeout {
        private int maxTimeToLiveInSeconds;

        public int getMaxTimeToLiveInSeconds() {
            return maxTimeToLiveInSeconds;
        }

        public void setMaxTimeToLiveInSeconds(final int maxTimeToLiveInSeconds) {
            this.maxTimeToLiveInSeconds = maxTimeToLiveInSeconds;
        }
    }

    
    public static class ThrottledTimeout {
        private long timeToKillInSeconds;
        private long timeInBetweenUsesInSeconds;

        public long getTimeToKillInSeconds() {
            return timeToKillInSeconds;
        }

        public void setTimeToKillInSeconds(final long timeToKillInSeconds) {
            this.timeToKillInSeconds = timeToKillInSeconds;
        }

        public long getTimeInBetweenUsesInSeconds() {
            return timeInBetweenUsesInSeconds;
        }

        public void setTimeInBetweenUsesInSeconds(final long timeInBetweenUsesInSeconds) {
            this.timeInBetweenUsesInSeconds = timeInBetweenUsesInSeconds;
        }
    }
}
