package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link AdaptiveAuthenticationIPIntelligenceProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
public class AdaptiveAuthenticationIPIntelligenceProperties implements Serializable {

    private static final long serialVersionUID = -9111174229142982880L;

    /**
     * Fetch IP diagnostic information via REST.
     */
    private Rest rest = new Rest();

    @Getter
    @Setter
    public static class Rest extends RestEndpointProperties {
        private static final long serialVersionUID = 3659099897056632608L;
    }
}
