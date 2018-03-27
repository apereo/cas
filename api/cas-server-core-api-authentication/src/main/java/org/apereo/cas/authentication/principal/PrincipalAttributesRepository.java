package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Map;

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
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface PrincipalAttributesRepository extends Serializable {

    /**
     * Gets attributes for the given principal id.
     *
     * @param p the principal whose attributes need to be retrieved.
     * @return the attributes
     */
    Map<String, Object> getAttributes(Principal p);
}
