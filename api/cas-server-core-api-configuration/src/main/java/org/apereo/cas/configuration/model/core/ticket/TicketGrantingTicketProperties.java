package org.apereo.cas.configuration.model.core.ticket;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link TicketGrantingTicketProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
public class TicketGrantingTicketProperties implements Serializable {

    private static final long serialVersionUID = 2349079252583399336L;
    /**
     * Maximum length of TGTs.
     */
    private int maxLength = 50;

    /**
     * Maximum time in seconds TGTs would be live in CAS server.
     */
    private int maxTimeToLiveInSeconds = 28_800;

    /**
     * Time in seconds after which TGTs would be destroyed after a period of inactivity.
     */
    private int timeToKillInSeconds = 7_200;

    /**
     * Flag to control whether to track most recent SSO sessions.
     * As multiple tickets may be issued for the same application, this impacts
     * how session information is tracked for every ticket which then
     * has a subsequent impact on logout.
     */
    private boolean onlyTrackMostRecentSession = true;

    /**
     * Hard timeout for TGTs.
     */
    private HardTimeout hardTimeout = new HardTimeout();

    /**
     * Throttled timeout for TGTs.
     */
    private ThrottledTimeout throttledTimeout = new ThrottledTimeout();

    /**
     * Timeout for TGTs.
     */
    private Timeout timeout = new Timeout();

    /**
     * Remember me for TGTs.
     */
    private RememberMe rememberMe = new RememberMe();

    public RememberMe getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(final RememberMe rememberMe) {
        this.rememberMe = rememberMe;
    }

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

    public static class HardTimeout implements Serializable {
        private static final long serialVersionUID = 4160963910346416908L;
        /**
         * Timeout in seconds to kill the session and consider tickets expired.
         */
        private long timeToKillInSeconds;

        public long getTimeToKillInSeconds() {
            return timeToKillInSeconds;
        }

        public void setTimeToKillInSeconds(final long timeToKillInSeconds) {
            this.timeToKillInSeconds = timeToKillInSeconds;
        }
    }

    public static class Timeout implements Serializable {

        private static final long serialVersionUID = 8635419913795245907L;
        /**
         * Maximum time in seconds. for TGTs to be live in CAS server.
         */
        private int maxTimeToLiveInSeconds;

        public int getMaxTimeToLiveInSeconds() {
            return maxTimeToLiveInSeconds;
        }

        public void setMaxTimeToLiveInSeconds(final int maxTimeToLiveInSeconds) {
            this.maxTimeToLiveInSeconds = maxTimeToLiveInSeconds;
        }
    }
    
    public static class ThrottledTimeout implements Serializable {
        private static final long serialVersionUID = -2370751379747804646L;
        /**
         * Timeout in seconds to kill the session and consider tickets expired.
         */
        private long timeToKillInSeconds;
        /**
         * Timeout in between each attempt.
         */
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
    
    public static class RememberMe implements Serializable {

        private static final long serialVersionUID = 1899959269597512610L;
        /**
         * Flag to indicate whether remember-me facility is enabled.
         */
        private boolean enabled;

        /**
         * Time in seconds after which remember-me enabled SSO session will be destroyed.
         */
        private long timeToKillInSeconds = 1_209_600;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public long getTimeToKillInSeconds() {
            return timeToKillInSeconds;
        }

        public void setTimeToKillInSeconds(final long timeToKillInSeconds) {
            this.timeToKillInSeconds = timeToKillInSeconds;
        }
    }
}
