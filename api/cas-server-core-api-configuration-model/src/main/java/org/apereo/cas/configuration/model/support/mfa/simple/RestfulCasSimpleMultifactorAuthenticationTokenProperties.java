package org.apereo.cas.configuration.model.support.mfa.simple;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link RestfulCasSimpleMultifactorAuthenticationTokenProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiresModule(name = "cas-server-support-simple-mfa")
@Getter
@Setter
@Accessors(chain = true)

public class RestfulCasSimpleMultifactorAuthenticationTokenProperties extends RestEndpointProperties {
    @Serial
    private static final long serialVersionUID = -6333748853833491119L;
}
