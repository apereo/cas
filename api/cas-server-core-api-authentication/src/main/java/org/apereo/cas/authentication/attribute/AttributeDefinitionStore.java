package org.apereo.cas.authentication.attribute;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link AttributeDefinitionStore}.
 *
 * @author Misagh Moayyed
 * @author Travis Schmidt
 * @since 6.2.0
 */
public interface AttributeDefinitionStore {

    /**
     * Register attribute definition attribute.
     *
     * @param defn the defn
     * @return the attribute definition store
     */
    AttributeDefinitionStore registerAttributeDefinition(AttributeDefinition defn);

    /**
     * Locate attribute definition.
     *
     * @param name the name
     * @return the optional
     */
    Optional<AttributeDefinition> locateAttributeDefinition(String name);

    /**
     * Gets attribute definitions.
     *
     * @return the attribute definitions
     */
    Collection<AttributeDefinition> getAttributeDefinitions();

    /**
     * Gets attribute values.
     *
     * @param key    the key
     * @param values the values
     * @return the attribute values
     */
    Optional<List<Object>> getAttributeValues(String key, Map<String, List<Object>> values);
}
