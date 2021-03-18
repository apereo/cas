package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Pac4jDelegatedAuthenticationTwitterProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jDelegatedAuthenticationTwitterProperties extends Pac4jIdentifiableClientProperties {

    private static final long serialVersionUID = 6906343970517008092L;

    /**
     * Set to true to request the user's email address from the Twitter API.
     * For this to have an effect it must first be enabled in the Twitter developer console.
     */
    private boolean includeEmail;

    public Pac4jDelegatedAuthenticationTwitterProperties() {
        setClientName("Twitter");
    }
}
