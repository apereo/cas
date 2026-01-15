package org.apereo.cas.util.spring;

import module java.base;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.actuate.endpoint.Operation;
import org.springframework.boot.actuate.endpoint.annotation.DiscoveredEndpoint;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoint;

/**
 * This is {@link RestActuatorControllerEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface RestActuatorControllerEndpoint extends PathMappedEndpoint, DiscoveredEndpoint<@NonNull Operation> {
}
