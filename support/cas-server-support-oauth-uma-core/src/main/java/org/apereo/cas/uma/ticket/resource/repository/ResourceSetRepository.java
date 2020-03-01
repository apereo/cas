package org.apereo.cas.uma.ticket.resource.repository;

import org.apereo.cas.uma.ticket.resource.ResourceSet;

import java.util.Collection;
import java.util.Optional;

/**
 * This is {@link ResourceSetRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public interface ResourceSetRepository {

    /**
     * Gets all.
     *
     * @return the all
     */
    Collection<? extends ResourceSet> getAll();

    /**
     * Gets by id.
     *
     * @param id the id
     * @return the by id
     */
    Optional<ResourceSet> getById(long id);

    /**
     * Gets by owner.
     *
     * @param owner the owner
     * @return the by owner
     */
    Collection<ResourceSet> getByOwner(String owner);

    /**
     * Gets by client.
     *
     * @param clientId the client id
     * @return the by client
     */
    Collection<ResourceSet> getByClient(String clientId);

    /**
     * Save resource set or update if already found.
     *
     * @param set the set
     * @return the resource set
     */
    ResourceSet save(ResourceSet set);

    /**
     * Update resource set.
     *
     * @param currentResource the current resource
     * @param newResource     the new resource
     * @return the resource set
     */
    ResourceSet update(ResourceSet currentResource, ResourceSet newResource);

    /**
     * Remove.
     *
     * @param set the set
     */
    void remove(ResourceSet set);

    /**
     * Remove all.
     */
    void removeAll();

    /**
     * Count long.
     *
     * @return the long
     */
    long count();
}
