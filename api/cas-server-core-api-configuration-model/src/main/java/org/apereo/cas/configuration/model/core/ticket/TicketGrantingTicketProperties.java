package org.apereo.cas.configuration.model.core.ticket;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("TicketGrantingTicketProperties")
public class TicketGrantingTicketProperties implements Serializable {

    private static final long serialVersionUID = 2349079252583399336L;

    /**
     * Primary/default expiration policy settings.
     */
    @NestedConfigurationProperty
    private PrimaryTicketExpirationPolicyProperties primary = new PrimaryTicketExpirationPolicyProperties();

    /**
     * Core/common settings.
     */
    @NestedConfigurationProperty
    private TicketGrantingTicketCoreProperties core = new TicketGrantingTicketCoreProperties();

    /**
     * Hard timeout for tickets.
     */
    @NestedConfigurationProperty
    private HardTimeoutTicketExpirationPolicyProperties hardTimeout =
        new HardTimeoutTicketExpirationPolicyProperties();

    /**
     * Throttled timeout for tickets.
     */
    @NestedConfigurationProperty
    private ThrottledTimeoutTicketExpirationPolicyProperties throttledTimeout =
        new ThrottledTimeoutTicketExpirationPolicyProperties();

    /**
     * Timeout for tickets.
     */
    @NestedConfigurationProperty
    private TimeoutTicketExpirationPolicyProperties timeout =
        new TimeoutTicketExpirationPolicyProperties();

    /**
     * Remember me for tickets.
     */
    @NestedConfigurationProperty
    private RememberMeAuthenticationProperties rememberMe =
        new RememberMeAuthenticationProperties();

}
