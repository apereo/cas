package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.services.query.RegisteredServiceQuery;

/**
 * This is {@link RegisteredServiceIndexService}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public interface RegisteredServiceIndexService {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "registeredServiceIndexService";

    /**
     * Clear.
     */
    void clear();

    /**
     * Count indexed services.
     *
     * @return the int
     */
    int count();


    /**
     * Initialize.
     */
    void initialize();

    /**
     * Index services, replacing the services indexed so far. Implementations should add or
     * update the given services before removing the ones that are gone, so that queries
     * running at the same time do not miss services that remain.
     *
     * @param values the values
     */
    void indexServices(Collection<RegisteredService> values);

    /**
     * Find service by id.
     *
     * @param id the id
     * @return the optional
     */
    Optional<RegisteredService> findServiceBy(long id);

    /**
     * Find service by queries..
     *
     * @param queries the queries
     * @return the stream
     */
    Stream<RegisteredService> findServiceBy(RegisteredServiceQuery... queries);

    /**
     * Determines whether indexing is enabled.
     *
     * @return true/false
     */
    boolean isEnabled();

    /**
     * Index service.
     *
     * @param service the service
     */
    void indexService(RegisteredService service);

    /**
     * Apply query against registered service and find a match.
     *
     * @param registeredService the registered service
     * @param query             the query
     * @return true/false
     */
    boolean matches(RegisteredService registeredService, RegisteredServiceQuery query);
}
