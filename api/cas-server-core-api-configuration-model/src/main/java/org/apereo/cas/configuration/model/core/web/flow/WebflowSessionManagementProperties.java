package org.apereo.cas.configuration.model.core.web.flow;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link WebflowSessionManagementProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-webflow")
@Accessors(chain = true)
public class WebflowSessionManagementProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 7479028707118198914L;

    /**
     * Control server-side session storage.
     */
    @NestedConfigurationProperty
    private WebflowServerSessionsProperties server = new WebflowServerSessionsProperties();

    /**
     * Controls whether spring webflow sessions are to be stored server-side or client side.
     * By default state is managed on the client side, that is also signed and encrypted.
     */
    @RequiredProperty
    private boolean storage;

    /**
     * Controls whether the webflow session is pinned
     * to the client's IP address and user-agent.
     */
    private boolean pinToSession;
}
