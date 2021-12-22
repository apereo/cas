package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
@JsonFilter("Pac4jDelegatedAuthenticationScimProvisioningProperties")
public class Pac4jDelegatedAuthenticationScimProvisioningProperties implements Serializable {
    private static final long serialVersionUID = -1102345678378393382L;

    /**
     * Whether provisioning to SCIM targets should be enabled
     * for delegated authentication attempts.
     */
    private boolean enabled;
}
