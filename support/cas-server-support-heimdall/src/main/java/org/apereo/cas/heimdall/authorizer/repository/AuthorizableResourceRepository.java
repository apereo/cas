package org.apereo.cas.heimdall.authorizer.repository;

import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.springframework.beans.factory.DisposableBean;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * Find resource for namespace by id.
     *
     * @param namespace the namespace
     * @param id        the id
     * @return the optional
     */
    Optional<AuthorizableResource> find(String namespace, long id);
}
