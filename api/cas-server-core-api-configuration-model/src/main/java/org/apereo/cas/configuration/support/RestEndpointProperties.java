package org.apereo.cas.configuration.support;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RestEndpointProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-util", automated = true)
@Accessors(chain = true)
public class RestEndpointProperties extends BaseRestEndpointProperties {
    private static final long serialVersionUID = 2687020856160473089L;

    /**
     * HTTP method to use when contacting the rest endpoint.
     * Examples include {@code GET, POST}, etc.
     */
    @RequiredProperty
    private String method = "GET";
}
