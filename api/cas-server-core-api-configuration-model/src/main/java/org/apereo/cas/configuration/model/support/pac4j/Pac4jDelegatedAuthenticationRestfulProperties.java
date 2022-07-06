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

    /**
     * Control the size of the delegated identity provider cache
     * that holds identity providers.
     * 
     * This setting specifies the maximum number of entries the cache may contain. Note that the cache <b>may evict
     * an entry before this limit is exceeded or temporarily exceed the threshold while evicting</b>.
     * As the cache size grows close to the maximum, the cache evicts entries that are less likely to
     * be used again. For example, the cache may evict an entry because it hasn't been used recently
     * or very often.
     */
    private long cacheSize = 100;

    /**
     * Specify the format of the payload that would be produced by the REST API.
     * Accepted values are:
     * <ul>
     *     <li>{@code pac4j}: The output must confirm to the syntax controlled by pac4j's {@code PropertiesConfigFactory}</li>
     *     <li>{@code cas}: The output must should contain properties that allow CAS to build delegated identity providers.</li>
     * </ul>
     */
    private String type = "pac4j";
}
