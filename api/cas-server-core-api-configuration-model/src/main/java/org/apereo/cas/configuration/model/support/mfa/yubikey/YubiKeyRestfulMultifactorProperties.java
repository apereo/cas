package org.apereo.cas.configuration.model.support.mfa.yubikey;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("YubiKeyRestfulMultifactorProperties")
public class YubiKeyRestfulMultifactorProperties extends RestEndpointProperties {
    private static final long serialVersionUID = -33291036299848782L;
}
