package org.apereo.cas.configuration.model.support.mfa.u2f;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link U2FRestfulMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-u2f")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("U2FRestfulMultifactorAuthenticationProperties")
public class U2FRestfulMultifactorAuthenticationProperties extends RestEndpointProperties {

    private static final long serialVersionUID = -8102345678378393382L;
}
