package org.apereo.cas.configuration.model.support.mfa.trusteddevice;

import module java.base;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link JpaTrustedDevicesMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-trusted-mfa-jdbc")

public class JpaTrustedDevicesMultifactorProperties extends AbstractJpaProperties {
    @Serial
    private static final long serialVersionUID = -8329950619696176349L;
}
