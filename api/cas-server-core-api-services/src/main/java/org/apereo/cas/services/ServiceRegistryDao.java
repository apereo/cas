package org.apereo.cas.services;

import java.util.List;

/**
 * Registry of all RegisteredServices.
 *
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @since 3.1
 */
public interface ServiceRegistryDao {

    /**
     * Persist the service in the data store.
     *
     * @param registeredService the service to persist.
     * @return the updated RegisteredService.
     */
    RegisteredService save(RegisteredService registeredService);

    /**
     * Remove the service from the data store.
     *
     * @param registeredService the service to remove.
     * @return true if it was removed, false otherwise.
     */
    boolean delete(RegisteredService registeredService);

    /**
     * Retrieve the services from the data store.
     *
     * @return the collection of services.
     */
    List<RegisteredService> load();

    /**
     * Find service by the numeric id.
     *
     * @param id the id
     * @return the registered service
     */
    RegisteredService findServiceById(long id);

    /**
     * Find service by the service id.
     *
     * @param id the id
     * @return the registered service
     */
    RegisteredService findServiceById(String id);

    /**
     * Find a service by an exact match of the service id.
     *
     * @param id the id
     * @return the registered service
     */
    RegisteredService findServiceByExactServiceId(String id);

    /**
     * Return number of records held in this service registry. Provides Java 8 supported default implementation so that implementations
     * needed this new functionality could override it and other implementations not caring for it could be left alone.
     *
     * @return number of registered services held by any particular implementation
     * @since 5.0.0
     */
    long size();

    /**
     * Returns the friendly name of this registry.
     *
     * @return the name.
     * @since 5.2.0
     */
    String getName();
}
