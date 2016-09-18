package org.apereo.cas.authentication.principal;

import java.io.Serializable;
import java.util.Map;

/**
 * Defines operations to create principals.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public interface PrincipalFactory extends Serializable {
    /**
     * Create principal.
     *
     * @param id the id
     * @return the principal
     */
    Principal createPrincipal(String id);

    /**
     * Create principal along with its attributes.
     *
     * @param id the id
     * @param attributes the attributes
     * @return the principal
     */
    Principal createPrincipal(String id, Map<String, Object> attributes);

}
