package org.jasig.cas.services;

import java.util.Collection;

import org.jasig.cas.authentication.principal.Service;

/**
 * Manages the storage, retrieval, and matching of Services wishing to use CAS
 * and services that have been registered with CAS.
 *
 * @author Scott Battaglia

 * @since 3.1
 */
public interface ServicesManager {

    /**
     * Register a service with CAS, or update an existing an entry.
     *
     * @param registeredService the RegisteredService to update or add.
     * @return newly persisted RegisteredService instance
     */
    RegisteredService save(RegisteredService registeredService);

    /**
     * Delete the entry for this RegisteredService.
     *
     * @param id the id of the registeredService to delete.
     * @return the registered service that was deleted, null if there was none.
     */
    RegisteredService delete(long id);

    /**
     * Find a RegisteredService by matching with the supplied service.
     *
     * @param service the service to match with.
     * @return the RegisteredService that matches the supplied service.
     */
    RegisteredService findServiceBy(Service service);

    /**
     * Find a RegisteredService by matching with the supplied id.
     *
     * @param id the id to match with.
     * @return the RegisteredService that matches the supplied service.
     */
    RegisteredService findServiceBy(long id);

    /**
     * Retrieve the collection of all registered services.
     *
     * @return the collection of all services.
     */
    Collection<RegisteredService> getAllServices();

    /**
     * Convenience method to let one know if a service exists in the data store.
     *
     * @param service the service to check.
     * @return true if it exists, false otherwise.
     */
    boolean matchesExistingService(Service service);
}
