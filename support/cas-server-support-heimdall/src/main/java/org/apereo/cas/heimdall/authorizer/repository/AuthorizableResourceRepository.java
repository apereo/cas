package org.apereo.cas.heimdall.authorizer.repository;

import module java.base;
import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResources;
import org.apereo.cas.heimdall.authzen.AuthZenAction;
import org.apereo.cas.heimdall.authzen.AuthZenResource;
import org.springframework.beans.factory.DisposableBean;

/**
 * This is {@link AuthorizableResourceRepository}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public interface AuthorizableResourceRepository extends DisposableBean {

    /**
     * The bean name.
     */
    String BEAN_NAME = "authorizableResourceRepository";
    
    /**
     * Find all resources.
     *
     * @return the map of resources
     */
    Map<String, List<AuthorizableResource>> findAll();

    @Override
    default void destroy() {
    }

    /**
     * Find authorizable resource.
     *
     * @param request the request
     * @return the authorizable resource
     */
    Optional<AuthorizableResource> find(AuthorizationRequest request);

    /**
     * Find list of resources by namespace.
     *
     * @param namespace the namespace
     * @return the list
     */
    List<AuthorizableResource> find(String namespace);

    /**
     * Find resources in every namespace that support the AuthZEN resource and action.
     *
     * @param resource the AuthZEN resource
     * @param action   the AuthZEN action
     * @return the matching resources
     */
    default List<AuthorizableResource> find(final AuthZenResource resource, final AuthZenAction action) {
        return findAll()
            .values()
            .stream()
            .flatMap(List::stream)
            .filter(entry -> entry.supports(resource, action))
            .toList();
    }

    /**
     * Find resource for namespace by id.
     *
     * @param namespace the namespace
     * @param id        the id
     * @return the optional
     */
    Optional<AuthorizableResource> find(String namespace, long id);

    /**
     * Store authorizable resources.
     *
     * @param resource the resources
     * @return the authorizable resource
     */
    AuthorizableResources store(AuthorizableResources resource);
}
