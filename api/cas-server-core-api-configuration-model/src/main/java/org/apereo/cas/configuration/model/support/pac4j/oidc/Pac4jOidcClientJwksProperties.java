package org.apereo.cas.configuration.model.support.pac4j.oidc;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link Pac4jOidcClientJwksProperties}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jOidcClientJwksProperties extends SpringResourceProperties {
    @Serial
    private static final long serialVersionUID = 5192010226236750446L;

    /**
     * The key identifier.
     */
    private String kid;
}
