package org.apereo.cas.configuration.model.support.pac4j.oidc;

import lombok.Getter;
import lombok.Setter;
import org.apereo.cas.configuration.support.RequiresModule;

/**
 * This is {@link Pac4jGoogleOidcClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
public class Pac4jGoogleOidcClientProperties extends BasePac4jOidcClientProperties {
    private static final long serialVersionUID = 3259382317533639638L;
}
