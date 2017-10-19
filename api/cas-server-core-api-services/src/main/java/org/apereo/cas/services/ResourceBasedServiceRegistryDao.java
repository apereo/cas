package org.apereo.cas.services;

import java.io.File;
import java.util.Collection;

/**
 * This is {@link ResourceBasedServiceRegistryDao},
 * which describes operations relevant to a service registry
 * that is backed by file-system resources.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface ResourceBasedServiceRegistryDao extends ServiceRegistryDao {

    /**
     * Update a single service instance.
     *
     * @param service the service
     */
    void update(RegisteredService service);

    /**
     * Load registered service from the given file.
     *
     * @param file the file
     * @return the registered services
     */
    Collection<RegisteredService> load(File file);
}
