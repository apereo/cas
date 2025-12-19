package org.apereo.cas.authentication.principal;

import module java.base;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public interface RegisteredServicePrincipalAttributesRepository extends Serializable {
    /**
     * Logger instance.
     */
    Logger LOGGER = LoggerFactory.getLogger(RegisteredServicePrincipalAttributesRepository.class);

    /**
     * Gets attributes for the given principal id.
     *
     * @param context the context
     * @return the attributes
     */
    Map<String, List<Object>> getAttributes(RegisteredServiceAttributeReleasePolicyContext context);

    /**
     * Gets attribute repository ids that should be used to
     * fetch attributes. An empty collection indicates
     * that all sources available and defined should be used.
     *
     * @return the attribute repository ids
     */
    Set<String> getAttributeRepositoryIds();

    /**
     * Add principal attributes into the underlying cache instance.
     *
     * @param id         identifier used by the cache as key.
     * @param attributes attributes to cache
     * @param context    the context
     * @since 4.2
     */
    default void update(final String id, final Map<String, List<Object>> attributes, final RegisteredServiceAttributeReleasePolicyContext context) {
        LOGGER.debug("Using [{}], no caching/update takes place for [{}] to add attributes [{}]", id, getClass().getSimpleName(), attributes);
    }
}
