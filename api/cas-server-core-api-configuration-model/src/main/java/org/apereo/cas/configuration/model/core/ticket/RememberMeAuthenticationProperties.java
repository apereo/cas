package org.apereo.cas.configuration.model.core.ticket;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
@JsonFilter("RememberMeTicketExpirationPolicyProperties")
public class RememberMeAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = 1899959269597512610L;

    /**
     * Flag to indicate whether remember-me facility is enabled.
     */
    private boolean enabled;

    /**
     * Time in seconds after which remember-me enabled SSO session will be destroyed.
     */
    private long timeToKillInSeconds = 1_209_600;

    /**
     * Regular expression that, when defined,
     * forces CAS to create a remember-me authentication
     * session if the current user-agent matches this pattern.
     * If a match is not found, remember-me is ignored.
     * If left undefined, remember-me authentication
     * will proceed with the default CAS behavior.
     */
    private String supportedUserAgents;

    /**
     * Regular expression that, when defined,
     * forces CAS to create a remember-me authentication
     * session if the current client ip (remote) address matches this pattern.
     * If a match is not found, remember-me is ignored.
     * If left undefined, remember-me authentication
     * will proceed with the default CAS behavior.
     */
    private String supportedIpAddresses;
}
