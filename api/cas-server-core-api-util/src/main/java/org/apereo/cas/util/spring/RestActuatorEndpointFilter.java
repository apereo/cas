package org.apereo.cas.util.spring;

import module java.base;
import org.springframework.boot.actuate.endpoint.annotation.DiscovererEndpointFilter;

/**
 * This is {@link RestActuatorEndpointFilter}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class RestActuatorEndpointFilter extends DiscovererEndpointFilter {
    RestActuatorEndpointFilter() {
        super(RestActuatorEndpointDiscoverer.class);
    }
}
