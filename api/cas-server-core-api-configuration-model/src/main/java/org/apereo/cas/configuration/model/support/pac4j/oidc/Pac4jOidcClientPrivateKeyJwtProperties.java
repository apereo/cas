package org.apereo.cas.configuration.model.support.pac4j.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;

/**
 * This is {@link Pac4jOidcClientPrivateKeyJwtProperties}.
 *
 * @author Jerome Leleu
 * @since 8.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jOidcClientPrivateKeyJwtProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 5192010226236750446L;

    /**
     * Specific JWKS for the private_key_jwt client authentication method.
     */
    @NestedConfigurationProperty
    private Pac4jOidcClientJwksProperties jwks = new Pac4jOidcClientJwksProperties();
}
