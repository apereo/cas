package org.apereo.cas.configuration.model.support.mfa.webauthn;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link WebAuthnRestfulMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-webauthn-rest")
@Getter
@Setter
@Accessors(chain = true)
public class WebAuthnRestfulMultifactorProperties extends RestEndpointProperties {
    @Serial
    private static final long serialVersionUID = -77291036299848782L;
}

