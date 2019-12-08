package org.apereo.cas.authentication.principal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines operations to create principals.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@FunctionalInterface
public interface PrincipalFactory extends Serializable {
    /**
     * Create principal.
     *
     * @param id the id
     * @return the principal
     */
    default Principal createPrincipal(final String id) {
        return createPrincipal(id, new HashMap<>(0));
    }

    /**
     * Create principal along with its attributes.
     *
     * @param id         the id
     * @param attributes the attributes
     * @return the principal
     */
    Principal createPrincipal(String id, Map<String, List<Object>> attributes);
}
