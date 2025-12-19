package org.apereo.cas.configuration.model.core.ticket;

import module java.base;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RememberMeAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Setter
@Accessors(chain = true)
public class RememberMeAuthenticationProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 1899959269597512610L;

    /**
     * Flag to indicate whether remember-me facility is enabled.
     */
    private boolean enabled;

    /**
     * Time in seconds after which remember-me enabled SSO session will be destroyed.
     */
    @DurationCapable
    private String timeToKillInSeconds = "P14D";

    /**
     * Regular expression that, when defined,
     * forces CAS to create a remember-me authentication
     * session if the current user-agent matches this pattern.
     * If a match is not found, remember-me is ignored.
     * If left undefined, remember-me authentication
     * will proceed with the default CAS behavior.
     */
    @RegularExpressionCapable
    private String supportedUserAgents;

    /**
     * Regular expression that, when defined,
     * forces CAS to create a remember-me authentication
     * session if the current client ip (remote) address matches this pattern.
     * If a match is not found, remember-me is ignored.
     * If left undefined, remember-me authentication
     * will proceed with the default CAS behavior.
     */
    @RegularExpressionCapable
    private String supportedIpAddresses;
}
