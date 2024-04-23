package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link Pac4jDelegatedAuthenticationLinkedInProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jDelegatedAuthenticationLinkedInProperties extends Pac4jIdentifiableClientProperties {

    @Serial
    private static final long serialVersionUID = -5663033494303169583L;

    /**
     * The requested scope.
     */
    private String scope;

    public Pac4jDelegatedAuthenticationLinkedInProperties() {
        setClientName("LinkedIn");
    }
}
