package org.apereo.cas.configuration.model.support.mfa;

import module java.base;
import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RestfulMultifactorAuthenticationProviderBypassProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class RestfulMultifactorAuthenticationProviderBypassProperties extends RestEndpointProperties {
    @Serial
    private static final long serialVersionUID = 1833594332973137011L;
}
