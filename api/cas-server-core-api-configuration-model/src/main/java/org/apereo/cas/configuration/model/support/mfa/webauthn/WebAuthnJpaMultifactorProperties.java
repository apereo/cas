package org.apereo.cas.configuration.model.support.mfa.webauthn;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link WebAuthnJpaMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-webauthn-jpa")
@Getter
@Setter
@Accessors(chain = true)
public class WebAuthnJpaMultifactorProperties extends AbstractJpaProperties {

    private static final long serialVersionUID = -4114840263678287815L;
}
