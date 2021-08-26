package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RestfulAdaptiveAuthenticationIPIntelligenceProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class RestfulAdaptiveAuthenticationIPIntelligenceProperties extends RestEndpointProperties {
    private static final long serialVersionUID = 3659099897056632608L;
}
