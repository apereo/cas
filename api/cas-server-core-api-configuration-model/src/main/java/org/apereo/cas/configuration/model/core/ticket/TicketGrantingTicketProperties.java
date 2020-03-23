package org.apereo.cas.configuration.model.core.ticket;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link TicketGrantingTicketProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Getter
@Setter
@Accessors(chain = true)
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

    @Getter
    @RequiresModule(name = "cas-server-core-tickets", automated = true)
    @Setter
    @Accessors(chain = true)
    public static class HardTimeout implements Serializable {

        private static final long serialVersionUID = 4160963910346416908L;

        /**
         * Timeout in seconds to kill the session and consider tickets expired.
         */
        private long timeToKillInSeconds;
    }

    @RequiresModule(name = "cas-server-core-tickets", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Timeout implements Serializable {

        private static final long serialVersionUID = 8635419913795245907L;

        /**
         * Maximum time in seconds. for TGTs to be live in CAS server.
         */
        private int maxTimeToLiveInSeconds;
    }

    @Getter
    @Setter
    @RequiresModule(name = "cas-server-core-tickets", automated = true)
    @Accessors(chain = true)
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
    }

    @Accessors(chain = true)
    @RequiresModule(name = "cas-server-core-tickets", automated = true)
    @Getter
    @Setter
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
    }
}
