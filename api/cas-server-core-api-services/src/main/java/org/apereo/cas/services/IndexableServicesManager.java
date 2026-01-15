package org.apereo.cas.services;

import module java.base;

/**
 * This is {@link IndexableServicesManager}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface IndexableServicesManager extends ServicesManager {

    /**
     * Count indexed services.
     *
     * @return the long
     */
    long countIndexedServices();

    /**
     * Find indexed service by id.
     *
     * @param id the id
     * @return the optional
     */
    Optional<RegisteredService> findIndexedServiceBy(long id);

    /**
     * Clear indexed services.
     */
    void clearIndexedServices();
}
