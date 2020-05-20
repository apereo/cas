package org.apereo.cas.authentication.attribute;

import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is {@link AttributeDefinitionStore}.
 *
 * @author Misagh Moayyed
 * @author Travis Schmidt
 * @since 6.2.0
 */
public interface AttributeDefinitionStore {
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
     * @return the attribute values
     */
    Optional<Pair<AttributeDefinition, List<Object>>> resolveAttributeValues(String key, List<Object> values,
                                                                             RegisteredService registeredService);

    /**
     * Gets attribute values.
     *
     * @param attributes        the values
     * @param registeredService the registered service
     * @return the attribute values
     */
    default Map<String, List<Object>> resolveAttributeValues(final Map<String, List<Object>> attributes,
                                                             final RegisteredService registeredService) {
        return attributes
            .entrySet()
            .stream()
            .map(entry -> {

                val defnResult = locateAttributeDefinition(entry.getKey());
                if (defnResult.isPresent()) {
                    val definition = defnResult.get();
                    val attributeValues = determineValuesForAttributeDefinition(attributes, entry, definition);
                    LOGGER.trace("Resolving attribute [{}] from attribute definition store with values [{}]", entry.getKey(), attributeValues);

                    val result = resolveAttributeValues(entry.getKey(), attributeValues, registeredService);
                    if (result.isPresent()) {
                        val resolvedValues = result.get().getValue();
                        LOGGER.trace("Resolving attribute [{}] based on attribute definition [{}]", entry.getKey(), definition);
                        val attributeKey = StringUtils.defaultIfBlank(definition.getName(), entry.getKey());
                        LOGGER.trace("Determined attribute name to be [{}] with values [{}]", attributeKey, resolvedValues);
                        return Pair.of(attributeKey, resolvedValues);
                    }
                }

                LOGGER.trace("Using already-resolved attribute name/value, as no attribute definition was found for [{}]", entry.getKey());
                return Pair.of(entry.getKey(), entry.getValue());
            })
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    /**
     * Determine if attribute definition store is empty.
     *
     * @return true/false
     */
    boolean isEmpty();
}
