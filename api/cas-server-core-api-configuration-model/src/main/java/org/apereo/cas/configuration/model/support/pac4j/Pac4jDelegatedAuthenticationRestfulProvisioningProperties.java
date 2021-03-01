package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Pac4jDelegatedAuthenticationRestfulProvisioningProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pac4j")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("Pac4jDelegatedAuthenticationRestfulProvisioningProperties")
public class Pac4jDelegatedAuthenticationRestfulProvisioningProperties extends RestEndpointProperties {
    private static final long serialVersionUID = -8102345678378393382L;
}
