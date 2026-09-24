package org.apereo.cas.configuration.model.core.services;

import module java.base;
import org.apereo.cas.configuration.model.BaseRestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.Ordered;

/**
 * This is {@link RestfulServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-rest-service-registry")
@Accessors(chain = true)
@Getter
@Setter
public class RestfulServiceRegistryProperties extends BaseRestEndpointProperties {
    @Serial
    private static final long serialVersionUID = 7086088180957285517L;

    /**
     * The execution order of this registry
     * which will determine its position in a chain
     * in case multiple registries are defined.
     */
    private int order = Ordered.LOWEST_PRECEDENCE;
}
