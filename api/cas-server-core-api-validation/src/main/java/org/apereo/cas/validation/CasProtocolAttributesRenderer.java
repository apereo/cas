package org.apereo.cas.validation;

import java.util.Collection;
import java.util.Map;

/**
 * This is {@link CasProtocolAttributesRenderer} that decides how cas protocol attributes
 * should be rendered in the final validation response. Implementations are free to choice the proper format
 * in the way that multi-valued attributes or prefixes are defined.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface CasProtocolAttributesRenderer {

    /**
     * Render attributes.
     *
     * @param attributes the attributes, expected to be finalized and encoded.
     * @return the collection
     */
    Collection<String> render(Map<String, Object> attributes);

}
