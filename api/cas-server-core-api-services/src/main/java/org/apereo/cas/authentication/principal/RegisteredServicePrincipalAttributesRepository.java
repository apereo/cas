package org.apereo.cas.authentication.principal;

import org.apereo.cas.services.RegisteredService;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines operations required for retrieving principal attributes.
 * Acts as a proxy between the external attribute source and CAS,
 * executing such as additional processing or caching on the set
 * of retrieved attributes. Implementations may simply decide to
 * do nothing on the set of attributes that the principal carries
 * or they may attempt to refresh them from the source, etc.
 *
 * @author Misagh Moayyed
 * @see PrincipalFactory
 * @since 4.1
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServicePrincipalAttributesRepository extends Serializable {

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
}
