package org.apereo.cas.configuration.model.support.pac4j.oidc;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

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
