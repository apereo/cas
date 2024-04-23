package org.apereo.cas.services;

import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Registry of all RegisteredServices.
 *
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @since 3.1
 */
public interface ServiceRegistry {

    /**
     * Bean name.
     */
    String BEAN_NAME = "serviceRegistry";

    /**
     * Persist the service in the data store.
     *
     * @param supplier       the supplier
     * @param andThenConsume the and then consume
     * @param countExclusive the count exclusive
     * @return the count of saved services
     */
    default Long save(final Supplier<RegisteredService> supplier,
                      final Consumer<RegisteredService> andThenConsume,
                      final long countExclusive) {
        return LongStream.range(0, countExclusive)
            .mapToObj(count -> supplier.get())
            .filter(Objects::nonNull)
            .map(this::save)
            .peek(andThenConsume)
            .count();
    }

    /**
     * Save.
     *
     * @param toSave the to save
     * @return the stream
     */
    default Stream<RegisteredService> save(final Stream<RegisteredService> toSave) {
        return toSave.map(this::save);
    }

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
     * Delete all services from the registry data store
     * and start clean.
     */
    void deleteAll();

    /**
     * Retrieve the services from the data store.
     *
     * @return the collection of services.
     */
    Collection<RegisteredService> load();

    /**
     * Gets services stream.
     * <p>
     * The returning stream may be bound to an IO channel (such as database connection),
     * so it should be properly closed after usage.
     *
     * @return the services stream
     */
    default Stream<? extends RegisteredService> getServicesStream() {
        return load().stream();
    }

    /**
     * Find service by the numeric id.
     *
     * @param id the id
     * @return the registered service
     */
    RegisteredService findServiceById(long id);

    /**
     * Find service by the numeric id.
     *
     * @param <T>   the type parameter
     * @param id    the id
     * @param clazz the clazz
     * @return the registered service
     */
    default <T extends RegisteredService> T findServiceById(final long id, final Class<T> clazz) {
        val service = findServiceById(id);
        if (service == null) {
            return null;
        }

        if (!clazz.isAssignableFrom(service.getClass())) {
            throw new ClassCastException("Object [" + service + " is of type " + service.getClass()
                                         + " when we were expecting " + clazz);
        }
        return clazz.cast(service);
    }

    /**
     * Find a service by matching with the service id.
     *
     * @param id the id to match with.
     * @return the registered service
     */
    default RegisteredService findServiceBy(final String id) {
        return getServicesStream()
            .sorted()
            .filter(r -> r.matches(id))
            .findFirst()
            .orElse(null);
    }

    /**
     * Find a service by an exact match of the service id.
     *
     * @param id the id
     * @return the registered service
     */
    default RegisteredService findServiceByExactServiceId(final String id) {
        return getServicesStream()
            .sorted()
            .filter(r -> StringUtils.isNotBlank(r.getServiceId()) && r.getServiceId().equals(id))
            .findFirst()
            .orElse(null);
    }

    /**
     * Find a service by an exact match of the service name.
     *
     * @param name the name
     * @return the registered service
     */
    default RegisteredService findServiceByExactServiceName(final String name) {
        return getServicesStream()
            .sorted()
            .filter(r -> r.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    /**
     * Find service by exact service name t.
     *
     * @param <T>   the type parameter
     * @param name  the name
     * @param clazz the clazz
     * @return the t
     */
    default <T extends RegisteredService> T findServiceByExactServiceName(final String name, final Class<T> clazz) {
        val service = findServiceByExactServiceName(name);
        if (service == null) {
            return null;
        }

        if (!clazz.isAssignableFrom(service.getClass())) {
            throw new ClassCastException("Object [" + service + " is of type " + service.getClass()
                                         + " when we were expecting " + clazz);
        }
        return clazz.cast(service);
    }

    /**
     * Find service predicate registered service.
     *
     * @param predicate the predicate
     * @return the registered service
     */
    default Collection<RegisteredService> findServicePredicate(final Predicate<RegisteredService> predicate) {
        return getServicesStream()
            .sorted()
            .filter(predicate)
            .collect(Collectors.toList());
    }

    /**
     * Return number of records held in this service registry. Provides default implementation so that implementations
     * needed this new functionality could override it and other implementations not caring for it could be left alone.
     *
     * @return number of registered services held by any particular implementation
     * @since 5.0.0
     */
    default long size() {
        return load().size();
    }

    /**
     * Returns the friendly name of this registry.
     *
     * @return the name.
     * @since 5.2.0
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
