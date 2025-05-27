package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.val;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface Principal extends Serializable {

    /**
     * Principal id.
     *
     * @return the unique id for the Principal
     */
    String getId();

    /**
     * Principal attributes.
     *
     * @return the map of configured attributes for this principal
     */
    default Map<String, List<Object>> getAttributes() {
        return new LinkedHashMap<>();
    }

    /**
     * Gets attribute.
     *
     * @param <T>          the type parameter
     * @param name         the name
     * @param expectedType the expected type
     * @return the attribute
     */
    @JsonIgnore
    default <T> T getSingleValuedAttribute(final String name, final Class<T> expectedType) {
        if (containsAttribute(name)) {
            val values = getAttributes().get(name);
            return values
                .stream()
                .filter(Objects::nonNull)
                .findFirst()
                .map(expectedType::cast)
                .orElse(null);
        }
        return null;
    }

    /**
     * Gets single valued attribute.
     *
     * @param name the name
     * @return the single valued attribute
     */
    @JsonIgnore
    default Object getSingleValuedAttribute(final String name) {
        return getSingleValuedAttribute(name, Object.class);
    }
    
    /**
     * Contains attribute by name.
     *
     * @param name the name
     * @return true or false
     */
    default boolean containsAttribute(final String name) {
        return getAttributes().containsKey(name);
    }
}
