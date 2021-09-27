package org.apereo.cas.authentication.principal;

import org.apereo.cas.services.RegisteredService;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines operations required for retrieving principal attributes.
 * Acts as a proxy between the external attribute source and CAS,
 * executing such as additional processing or caching on the set
 * of retrieved attributes. Implementations may decide to
 * do nothing on the set of attributes that the principal carries
 * or they may attempt to refresh them from the source, etc.
 *
 * @author Misagh Moayyed
 * @see PrincipalFactory
 * @since 4.1
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@FunctionalInterface
public interface RegisteredServicePrincipalAttributesRepository extends Serializable {
    Logger LOGGER = LoggerFactory.getLogger(RegisteredServicePrincipalAttributesRepository.class);

    /**
     * Gets attributes for the given principal id.
     *
     * @param principal         the principal whose attributes need to be retrieved.
     * @param registeredService the registered service
     * @return the attributes
     */
    Map<String, List<Object>> getAttributes(Principal principal, RegisteredService registeredService);

    /**
     * Gets attribute repository ids that should be used to
     * fetch attributes. An empty collection indicates
     * that all sources available and defined should be used.
     *
     * @return the attribute repository ids
     */
    default Set<String> getAttributeRepositoryIds() {
        return new HashSet<>(0);
    }

    /**
     * Add principal attributes into the underlying cache instance.
     *
     * @param id                identifier used by the cache as key.
     * @param attributes        attributes to cache
     * @param registeredService the registered service
     * @since 4.2
     */
    default void update(final String id, final Map<String, List<Object>> attributes, final RegisteredService registeredService) {
        LOGGER.debug("Using [{}], no caching/update takes place for [{}] to add attributes [{}]", id, getClass().getSimpleName(), attributes);
    }
}
