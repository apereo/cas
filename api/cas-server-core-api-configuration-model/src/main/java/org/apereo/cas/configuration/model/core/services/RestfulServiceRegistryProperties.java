package org.apereo.cas.configuration.model.core.services;

import org.apereo.cas.configuration.model.BaseRestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("RestfulServiceRegistryProperties")
public class RestfulServiceRegistryProperties extends BaseRestEndpointProperties {
    private static final long serialVersionUID = 7086088180957285517L;
}
