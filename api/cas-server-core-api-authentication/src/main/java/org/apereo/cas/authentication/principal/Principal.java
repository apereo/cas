package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic concept of an authenticated thing. Examples include a person or a
 * service.
 * <p>
 * The implementation SimplePrincipal just contains the Id property. More
 * complex Principal objects may contain additional information that are
 * meaningful to the View layer but are generally transparent to the rest of
 * CAS.
 * </p>
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface Principal extends Serializable {

    /**
     * @return the unique id for the Principal
     */
    String getId();

    /**
     * @return the map of configured attributes for this principal
     */
    default Map<String, Object> getAttributes() {
        return new LinkedHashMap<>(0);
    }
}
