package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link RestfulMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)

public class RestfulMultifactorAuthenticationProperties extends RestEndpointProperties {
    @Serial
    private static final long serialVersionUID = 3659099897056632608L;
}
