package org.apereo.cas.authentication.principal;

import org.apereo.cas.services.RegisteredService;

import java.util.List;
import java.util.Map;

/**
 * This is {@link PrincipalAttributesRepositoryCache}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface PrincipalAttributesRepositoryCache {
    /**
     * Default bean name.
     */
    String DEFAULT_BEAN_NAME = "principalAttributesRepositoryCache";

    /**
     * Invalidate cache contents.
     */
    void invalidate();

    /**
     * Fetch attributes.
     *
     * @param registeredService the registered service
     * @param repository        the repository
     * @param principal         the principal
     * @return the map
     */
    Map<String, List<Object>> fetchAttributes(RegisteredService registeredService,
                                              RegisteredServicePrincipalAttributesRepository repository,
                                              Principal principal);

    /**
     * Put attributes.
     *
     * @param registeredService the registered service
     * @param repository        the repository
     * @param id                the id
     * @param attributes        the attributes
     */
    void putAttributes(RegisteredService registeredService,
                       RegisteredServicePrincipalAttributesRepository repository,
                       String id, Map<String, List<Object>> attributes);
}
