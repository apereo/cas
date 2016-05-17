package org.apereo.cas.services;

import java.util.List;

/**
 * Registry of all RegisteredServices.
 *
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 *
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
     * Return number of records held in this service registry.
     *
     * Provides Java 8 supported default implementation so that implementations needed this new functionality could
     * override it and other implementations not caring for it could be left alone.
     *
     * The default implementation simply throws <code>UnsupportedOperationException</code>.
     *
     * @return number of registered services held by any particular implementation
     * @since 5.0.0
     */
    default long size() {
        throw new UnsupportedOperationException("This is the default implementation not supported by this service registry");
    }
}
