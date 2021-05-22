package org.apereo.cas.authentication.attribute;

import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * This is {@link AttributeDefinitionStore}.
 *
 * @author Misagh Moayyed
 * @author Travis Schmidt
 * @since 6.2.0
 */
public interface AttributeDefinitionStore {
    /**
     * Default bean name for the implementation class.
     */
    String BEAN_NAME = "attributeDefinitionStore";

    /**
     * The constant LOGGER.
     */
    Logger LOGGER = LoggerFactory.getLogger(AttributeDefinitionStore.class);

    private static List<Object> determineValuesForAttributeDefinition(final Map<String, List<Object>> attributes,
                                                                      final Map.Entry<String, List<Object>> entry,
                                                                      final AttributeDefinition definition) {
        val attributeKey = StringUtils.defaultIfBlank(definition.getAttribute(), entry.getKey());
        if (attributes.containsKey(attributeKey)) {
            return attributes.get(attributeKey);
        }
        return entry.getValue();
    }

    /**
     * Register attribute definition attribute.
     *
     * @param defn the defn
     * @return the attribute definition store
     */
    AttributeDefinitionStore registerAttributeDefinition(AttributeDefinition defn);

    /**
     * Register attribute definition attribute.
     *
     * @param key  the key
     * @param defn the defn
     * @return the attribute definition store
     */
    AttributeDefinitionStore registerAttributeDefinition(String key, AttributeDefinition defn);

    /**
     * Removes attribute definition attribute by key.
     *
     * @param key the key
     * @return the attribute definition store
     */
    AttributeDefinitionStore removeAttributeDefinition(String key);

    /**
     * Locate attribute definition.
     *
     * @param name the name
     * @return the optional
     */
    Optional<AttributeDefinition> locateAttributeDefinition(String name);

    /**
     * Locate attribute definition optional.
     *
     * @param <T>   the type parameter
     * @param key  the name
     * @param clazz the clazz
     * @return the optional
     */
    <T extends AttributeDefinition> Optional<T> locateAttributeDefinition(String key, Class<T> clazz);

    /**
     * Locate attribute definition.
     *
     * @param <T>       the type parameter
     * @param predicate the predicate
     * @return the optional
     */
    <T extends AttributeDefinition> Optional<T> locateAttributeDefinition(Predicate<AttributeDefinition> predicate);

    /**
     * Gets attribute definitions.
     *
     * @return the attribute definitions
     */
    Collection<AttributeDefinition> getAttributeDefinitions();

    /**
     * Gets attribute values.
     *
     * @param key               the key
     * @param values            the values
     * @param registeredService the registered service
     * @param attributes        the attributes
     * @return the attribute values
     */
    Optional<Pair<AttributeDefinition, List<Object>>> resolveAttributeValues(String key, List<Object> values,
                                                                             RegisteredService registeredService,
                                                                             Map<String, List<Object>> attributes);

    /**
     * Gets attribute values.
     *
     * @param attributes        the values
     * @param registeredService the registered service
     * @return the attribute values
     */
    default Map<String, List<Object>> resolveAttributeValues(final Map<String, List<Object>> attributes,
                                                             final RegisteredService registeredService) {
        val finalAttributes = new LinkedHashMap<String, List<Object>>(attributes.size());
        attributes
            .entrySet()
            .forEach(entry -> {
                locateAttributeDefinition(entry.getKey()).ifPresentOrElse(definition -> {
                    val attributeValues = determineValuesForAttributeDefinition(attributes, entry, definition);
                    LOGGER.trace("Resolving attribute [{}] from attribute definition store with values [{}]", entry.getKey(), attributeValues);
                    val result = resolveAttributeValues(entry.getKey(), attributeValues, registeredService, attributes);
                    if (result.isPresent()) {
                        val resolvedValues = result.get().getValue();
                        if (!resolvedValues.isEmpty()) {
                            LOGGER.trace("Resolving attribute [{}] based on attribute definition [{}]", entry.getKey(), definition);
                            val attributeKeys = org.springframework.util.StringUtils.commaDelimitedListToSet(
                                StringUtils.defaultIfBlank(definition.getName(), entry.getKey()));
                            
                            attributeKeys.forEach(key -> {
                                LOGGER.trace("Determined attribute name to be [{}] with values [{}]", key, resolvedValues);
                                finalAttributes.put(key, resolvedValues);
                            });
                        } else {
                            LOGGER.warn("Unable to produce or determine attributes values for attribute definition [{}]", definition);
                        }
                    }
                }, () -> {
                    LOGGER.trace("Using already-resolved attribute name/value, as no attribute definition was found for [{}]", entry.getKey());
                    finalAttributes.put(entry.getKey(), entry.getValue());
                });
            });
        LOGGER.trace("Final collection of attributes resolved from attribute definition store is [{}]", finalAttributes);
        return finalAttributes;
    }

    /**
     * Determine if attribute definition store is empty.
     *
     * @return true/false
     */
    boolean isEmpty();
}
