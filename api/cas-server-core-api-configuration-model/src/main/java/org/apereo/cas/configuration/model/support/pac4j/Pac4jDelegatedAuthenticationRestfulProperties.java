package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link Pac4jDelegatedAuthenticationRestfulProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)

public class Pac4jDelegatedAuthenticationRestfulProperties extends RestEndpointProperties {
    @Serial
    private static final long serialVersionUID = 3659099897056632608L;

    /**
     * Specify the format of the payload that would be produced by the REST API.
     * Accepted values are:
     * <ul>
     *     <li>{@code pac4j}: The output must confirm to the syntax controlled by pac4j's {@code PropertiesConfigFactory}</li>
     *     <li>{@code cas}: The output must should contain properties that allow CAS to build delegated identity providers.</li>
     * </ul>
     */
    private String type = "pac4j";
}
