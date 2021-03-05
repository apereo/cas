package org.apereo.cas.web;

import java.util.List;

/**
 * This is {@link ProtocolEndpointConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface ProtocolEndpointConfigurer {
    /**
     * Gets base endpoint.
     *
     * @return the base endpoint
     */
    List<String> getBaseEndpoints();
}
