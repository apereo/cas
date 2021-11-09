package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Pac4jDelegatedAuthenticationRestfulProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("Pac4jDelegatedAuthenticationRestfulProperties")
public class Pac4jDelegatedAuthenticationRestfulProperties extends RestEndpointProperties {
    private static final long serialVersionUID = 3659099897056632608L;

    /**
     * Control the expiration policy of the cache
     * that holds on the results from the rest api.
     */
    @DurationCapable
    private String cacheDuration = "PT8H";

}
