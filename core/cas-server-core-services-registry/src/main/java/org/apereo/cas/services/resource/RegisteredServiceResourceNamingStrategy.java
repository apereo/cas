package org.apereo.cas.services.resource;

import org.apereo.cas.services.RegisteredService;

/**
 * This is {@link RegisteredServiceResourceNamingStrategy}.  Interface to provide naming strategy
 * to resource based services.
 *
 * @author Travis Schmidt
 * @since 5.3.0
 */
@FunctionalInterface
public interface RegisteredServiceResourceNamingStrategy {

    /**
     * Method will be called to provide a name for a resource to store a service.
     *
     * @param service    - The Service to be saved.
     * @param extenstion - The extension to be used.
     * @return - String representing a resource name.
     */
    String build(RegisteredService service, String extenstion);
}
