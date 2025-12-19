package org.apereo.cas.configuration.model.support.mfa.trusteddevice;

import module java.base;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link JsonTrustedDevicesMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-trusted-mfa")

public class JsonTrustedDevicesMultifactorProperties extends SpringResourceProperties {
    @Serial
    private static final long serialVersionUID = -8690563713141571620L;
}
