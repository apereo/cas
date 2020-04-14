package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;

import lombok.val;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
     * Register a service with CAS, or update an existing an entry.
     *
     * @param registeredService the RegisteredService to update or add.
     * @param publishEvent      whether events should be published to indicate the save operation.
     * @return newly persisted RegisteredService instance
     */
    RegisteredService save(RegisteredService registeredService, boolean publishEvent);

    /**
     * Save collection of services.
     *
     * @param services the services
     */
    default void save(final RegisteredService... services) {
        Arrays.stream(services).forEach(this::save);
    }

    /**
     * Delete all entries in the underlying storage service.
     */
    void deleteAll();

    /**
     * Delete the entry for this RegisteredService.
     *
     * @param id the id of the registeredService to delete.
     * @return the registered service that was deleted, null if there was none.
     */
    RegisteredService delete(long id);

    /**
     * Delete the entry for this RegisteredService.
     *
     * @param svc the registered service to delete.
     * @return the registered service that was deleted, null if there was none.
     */
    RegisteredService delete(RegisteredService svc);

    /**
     * Find a RegisteredService by matching with the supplied service.
     *
     * @param serviceId the service to match with.
     * @return the RegisteredService that matches the supplied service.
     */
    RegisteredService findServiceBy(String serviceId);

    /**
     * Find a RegisteredService by matching with the supplied service.
     *
     * @param service the service to match with.
     * @return the RegisteredService that matches the supplied service.
     */
    RegisteredService findServiceBy(Service service);

    /**
     * Find a collection of services by type.
     *
     * @param clazz the clazz
     * @return the collection of services that matches the supplied type
     */
    Collection<RegisteredService> findServiceBy(Predicate<RegisteredService> clazz);

    /**
     * Find service by type.
     *
     * @param <T>       the type parameter
     * @param serviceId the service id
     * @param clazz     the clazz
     * @return the t
     */
    <T extends RegisteredService> T findServiceBy(Service serviceId, Class<T> clazz);

    /**
     * Find service by type.
     *
     * @param <T>       the type parameter
     * @param serviceId the service id
     * @param clazz     the clazz
     * @return the t
     */
    <T extends RegisteredService> T findServiceBy(String serviceId, Class<T> clazz);

    /**
     * Find a RegisteredService by matching with the supplied id.
     *
     * @param id the id to match with.
     * @return the RegisteredService that matches the supplied service.
     */
    RegisteredService findServiceBy(long id);

    /**
     * Find a RegisteredService by matching with the supplied id.
     *
     * @param <T>   the type parameter
     * @param id    the id to match with.
     * @param clazz the clazz
     * @return the RegisteredService that matches the supplied service.
     */
    default <T extends RegisteredService> T findServiceBy(final long id, final Class<T> clazz) {
        val service = findServiceBy(id);
        if (service != null && clazz.isAssignableFrom(service.getClass())) {
            return (T) service;
        }
        return null;
    }

    /**
     * Retrieve the collection of all registered services.
     * Services that are returned are valid, non-expired, etc.
     * Operation should perform no reloads, and must return a cached
     * copy of services that are already loaded.
     *
     * @return the collection of all services.
     */
    Collection<RegisteredService> getAllServices();

    /**
     * Gets services stream.
     * <p>
     * The returning stream may be bound to an IO channel (such as database connection),
     * so it should be properly closed after usage.
     *
     * @return the services stream
     */
    default Stream<? extends RegisteredService> getAllServicesStream() {
        return getAllServices().stream();
    }

    /**
     * Inform the ServicesManager to load or reload its list of services if its cached
     * them. Note that this is a suggestion and that ServicesManagers are free
     * to reload whenever they want.
     *
     * @return the collection
     */
    Collection<RegisteredService> load();

    /**
     * Return a count of loaded services by this manager.
     *
     * @return the count/size of registry.
     */
    default int count() {
        return 0;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Returns true if this manager supports the type of service passed.
     *
     * @param service - the service
     * @return - true if supported
     */
    default boolean supports(final Service service) {
        return true;
    }

    /**
     * Returns true if this manager supports the type of registered service.
     *
     * @param service - the registered service
     * @return - true if supported
     */
    default boolean supports(final RegisteredService service) {
        return true;
    }

    /**
     * Returns true if this manager supports the type of registered service.
     *
     * @param clazz - class types that are supported
     * @return - true if supported
     */
    default boolean supports(final Class clazz) {
        return true;
    }
}
