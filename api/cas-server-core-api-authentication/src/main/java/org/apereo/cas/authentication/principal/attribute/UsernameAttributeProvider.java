package org.apereo.cas.authentication.principal.attribute;

import java.util.List;
import java.util.Map;

/**
 * Provider for the username attribute to use when one is not otherwise provided.
 *
 * @author Eric Dalquist
 * @since 7.1.0
 */
public interface UsernameAttributeProvider {
    /**
     * Gets username attribute.
     *
     * @return The username attribute to use when one is not otherwise provided, will never return null.
     */
    String getUsernameAttribute();

    /**
     * Gets username from query.
     *
     * @param query The query map of attributes
     * @return The username included in the query, determined using the username attribute. Returns null if no username attribute is included in the query.
     */
    String getUsernameFromQuery(Map<String, List<Object>> query);
}
