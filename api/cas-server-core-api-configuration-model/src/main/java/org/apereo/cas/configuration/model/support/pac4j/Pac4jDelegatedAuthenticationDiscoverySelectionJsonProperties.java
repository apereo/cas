package org.apereo.cas.configuration.model.support.pac4j;

import module java.base;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Pac4jDelegatedAuthenticationDiscoverySelectionJsonProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jDelegatedAuthenticationDiscoverySelectionJsonProperties extends SpringResourceProperties {
    @Serial
    private static final long serialVersionUID = -2261947621312270068L;

    /**
     * The name of the principal attribute whose values will be compared against the key pattern defined in the configuration rules.
     * If a match is found, then the provider configuration block will be used as the selected provider.
     * The matching routine will examine all attribute values linked to the principal attribute to find any acceptable match.
     * When this setting left undefined, then the resolved principal id for the given user identifier will be used to locate the provider.
     */
    private String principalAttribute;
}
