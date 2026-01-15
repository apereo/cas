package org.apereo.cas.configuration.model.support.pac4j;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Pac4jDelegatedAuthenticationScimProvisioningProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-scim")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jDelegatedAuthenticationScimProvisioningProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -1102345678378393382L;

    /**
     * Whether provisioning to SCIM targets should be enabled
     * for delegated authentication attempts.
     */
    private boolean enabled;
}
