package org.apereo.cas.services;

/**
 * Manages the storage, retrieval, and matching of Services wishing to use CAS
 * and services that have been registered with CAS.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public interface ChainingServicesManager extends ServicesManager {

    /**
     * Adds a services manager to the chain.
     *
     * @param manager - a services manager
     */
    void registerServiceManager(ServicesManager manager);
}
