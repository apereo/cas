package org.apereo.cas.configuration.model.support.pac4j.oidc;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link Pac4jGoogleOidcClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-oidc")
@Getter
@Setter
@Accessors(chain = true)

public class Pac4jGoogleOidcClientProperties extends BasePac4jOidcClientProperties {
    @Serial
    private static final long serialVersionUID = 3259382317533639638L;

    public Pac4jGoogleOidcClientProperties() {
        setDiscoveryUri("https://accounts.google.com/.well-known/openid-configuration");
    }
}
