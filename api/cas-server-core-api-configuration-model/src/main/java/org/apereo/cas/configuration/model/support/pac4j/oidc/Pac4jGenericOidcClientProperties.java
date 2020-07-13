package org.apereo.cas.configuration.model.support.pac4j.oidc;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Pac4jGenericOidcClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jGenericOidcClientProperties extends BasePac4jOidcClientProperties {
    private static final long serialVersionUID = 3359382317533639638L;

    public Pac4jGenericOidcClientProperties() {
        setCallbackUrlType(CallbackUrlTypes.PATH_PARAMETER);
    }
}
