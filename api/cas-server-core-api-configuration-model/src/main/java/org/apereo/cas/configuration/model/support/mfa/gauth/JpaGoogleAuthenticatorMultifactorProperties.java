package org.apereo.cas.configuration.model.support.mfa.gauth;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link JpaGoogleAuthenticatorMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-gauth-jpa")
@Getter
@Setter
@Accessors(chain = true)
public class JpaGoogleAuthenticatorMultifactorProperties extends AbstractJpaProperties {
    private static final long serialVersionUID = -2689797889546802618L;
}

