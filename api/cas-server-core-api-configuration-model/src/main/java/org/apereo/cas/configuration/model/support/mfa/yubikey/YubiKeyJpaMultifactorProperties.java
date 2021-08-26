package org.apereo.cas.configuration.model.support.mfa.yubikey;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link YubiKeyJpaMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-yubikey-jpa")
@Getter
@Setter
@Accessors(chain = true)
public class YubiKeyJpaMultifactorProperties extends AbstractJpaProperties {
    private static final long serialVersionUID = -4420099402220880361L;
}
