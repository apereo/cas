package org.apereo.cas.services;

import java.io.File;
import java.nio.file.Watchable;

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
     * Gets the watchable resource.
     *
     * @param <T> the type parameter
     * @return the watchable resource
     */
    <T extends Watchable> T getWatchableResource();

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
     * @return the registered service
     */
    RegisteredService load(File file);
}
