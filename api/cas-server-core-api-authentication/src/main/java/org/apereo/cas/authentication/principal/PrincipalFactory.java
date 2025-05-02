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
     * Implementation bean name.
     */
    String BEAN_NAME = "principalFactory";

    /**
     * Create principal.
     *
     * @param id the id
     * @return the principal
     * @throws Throwable the throwable
     */
    default Principal createPrincipal(final String id) throws Throwable {
        return createPrincipal(id, new HashMap<>());
    }

    /**
     * Create principal along with its attributes.
     *
     * @param id         the id
     * @param attributes the attributes
     * @return the principal
     * @throws Throwable the throwable
     */
    Principal createPrincipal(String id, Map<String, List<Object>> attributes) throws Throwable;

    /**
     * Principal Without attributes.
     *
     * @param principal the principal
     * @return the principal
     * @throws Throwable the throwable
     */
    default Principal withoutAttributes(final Principal principal) throws Throwable {
        return createPrincipal(principal.getId());
    }
}
