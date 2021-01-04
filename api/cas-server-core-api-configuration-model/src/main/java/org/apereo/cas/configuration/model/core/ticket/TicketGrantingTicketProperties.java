package org.apereo.cas.configuration.model.core.ticket;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
    @NestedConfigurationProperty
    private HardTimeoutTicketExpirationPolicyProperties hardTimeout =
        new HardTimeoutTicketExpirationPolicyProperties();

    /**
     * Throttled timeout for TGTs.
     */
    @NestedConfigurationProperty
    private ThrottledTimeoutTicketExpirationPolicyProperties throttledTimeout =
        new ThrottledTimeoutTicketExpirationPolicyProperties();

    /**
     * Timeout for TGTs.
     */
    @NestedConfigurationProperty
    private TimeoutTicketExpirationPolicyProperties timeout =
        new TimeoutTicketExpirationPolicyProperties();

    /**
     * Remember me for TGTs.
     */
    @NestedConfigurationProperty
    private RememberMeAuthenticationProperties rememberMe =
        new RememberMeAuthenticationProperties();

}
