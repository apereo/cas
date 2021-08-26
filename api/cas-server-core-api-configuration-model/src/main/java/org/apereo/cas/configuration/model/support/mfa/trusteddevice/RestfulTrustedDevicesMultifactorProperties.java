package org.apereo.cas.configuration.model.support.mfa.trusteddevice;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RestfulTrustedDevicesMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-trusted-mfa-rest")
@JsonFilter("RestfulTrustedDevicesMultifactorProperties")
public class RestfulTrustedDevicesMultifactorProperties extends RestEndpointProperties {
    private static final long serialVersionUID = 3659099897056632608L;
}
