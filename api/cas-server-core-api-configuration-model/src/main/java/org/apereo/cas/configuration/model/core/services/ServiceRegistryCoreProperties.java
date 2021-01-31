package org.apereo.cas.configuration.model.core.services;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link ServiceRegistryCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-services", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("ServiceRegistryCoreProperties")
public class ServiceRegistryCoreProperties implements Serializable {
    private static final long serialVersionUID = -268826011744304210L;

    /**
     * Flag that indicates whether to initialise active service
     * registry implementation with a default set of service definitions included
     * with CAS by default in JSON format.
     * The initialization generally tends to find JSON service definitions
     * from {@link org.apereo.cas.configuration.model.support.services.json.JsonServiceRegistryProperties#getLocation()}.
     */
    private boolean initFromJson;

    /**
     * Determine how services are internally managed, queried, cached and reloaded by CAS.
     * Accepted values are the following:
     *
     * <ul>
     * <li>DEFAULT: Keep all services inside a concurrent map.</li>
     * <li>DOMAIN: Group registered services by their domain having been explicitly defined.</li>
     * </ul>
     */
    private ServiceManagementTypes managementType = ServiceManagementTypes.DEFAULT;

    /**
     * Types of service managers that one can control.
     */
    public enum ServiceManagementTypes {
        /**
         * Group service definitions by their domain.
         */
        DOMAIN,
        /**
         * Default option to keep definitions in a map as they arrive.
         */
        DEFAULT
    }
}
