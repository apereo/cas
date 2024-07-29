package org.apereo.cas.configuration.model.support.mfa.yubikey;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link YubiKeyRestfulMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-yubikey")
@Getter
@Setter
@Accessors(chain = true)

public class YubiKeyRestfulMultifactorProperties extends RestEndpointProperties {
    @Serial
    private static final long serialVersionUID = -33291036299848782L;
}
