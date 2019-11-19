package org.apereo.cas.services;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * This is {@link ServicesManagerExecutionPlan}.
 *
 * @author Travis Schmidt
 * @since 6.1.0
 */
public interface ServicesManagerExecutionPlan {
    /**
     * Register service manager.
     *
     * @param manager the manager
     * @return the service registry execution plan
     */
    ServicesManagerExecutionPlan registerServicesManager(ServicesManager manager);

    /**
     * Get service managers collection.
     *
     * @return the collection
     */
    Collection<ServicesManager> getServicesManagers();

    /**
     * Get service registries collection.
     *
     * @param typeFilter the type filter
     * @return the collection
     */
    Collection<ServicesManager> find(Predicate<ServicesManager> typeFilter);

}
