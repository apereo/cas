package org.apereo.cas.services.resource;

import org.apereo.cas.services.RegisteredService;

import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link DefaultRegisteredServiceResourceNamingStrategy}. This class provides the default
 * naming for resource based services.
 *
 * @author Travis Schmidt
 * @since 5.3.0
 */
public class DefaultRegisteredServiceResourceNamingStrategy implements RegisteredServiceResourceNamingStrategy {

    /**
     * Method creates a filename to store the service.
     *
     * @param service   - Service to be stored.
     * @param extension - extension to use for the file.
     * @return - String representing file name.
     */
    @Override
    public String build(final RegisteredService service, final String extension) {
        return StringUtils.remove(service.getName(), ' ') + '-' + service.getId() + '.' + extension;
    }
}
