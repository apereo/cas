package org.apereo.cas.configuration.model.support.mfa.webauthn;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link WebAuthnJsonMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-webauthn")
@Getter
@Setter
@Accessors(chain = true)
public class WebAuthnJsonMultifactorProperties extends SpringResourceProperties {
    private static final long serialVersionUID = -1283660787308509919L;
}
