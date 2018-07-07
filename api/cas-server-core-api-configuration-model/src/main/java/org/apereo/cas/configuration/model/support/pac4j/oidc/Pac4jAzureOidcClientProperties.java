package org.apereo.cas.configuration.model.support.pac4j.oidc;

import lombok.Getter;
import lombok.Setter;
import org.apereo.cas.configuration.support.RequiresModule;

/**
 * This is {@link Pac4jAzureOidcClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
public class Pac4jAzureOidcClientProperties extends BasePac4jOidcClientProperties {
    private static final long serialVersionUID = 1259382317533639638L;

    /**
     * Azure AD tenant name.
     */
    private String tenant;
}
