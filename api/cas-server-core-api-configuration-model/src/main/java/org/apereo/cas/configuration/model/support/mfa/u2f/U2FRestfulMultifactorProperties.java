package org.apereo.cas.configuration.model.support.mfa.u2f;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link U2FRestfulMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-u2f")
@Getter
@Setter
@Accessors(chain = true)
public class U2FRestfulMultifactorProperties extends RestEndpointProperties {

    private static final long serialVersionUID = -8102345678378393382L;
}
