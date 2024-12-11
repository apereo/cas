package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
     * Register attribute definition attribute.
     * The definition will only be added if one does not already exist in the store.
     *
     * @param defn the defn
     * @return the attribute definition store
     */
    AttributeDefinitionStore registerAttributeDefinition(AttributeDefinition defn);

    /**
     * Register attribute definition attribute.
     * The definition will only be added if one does not already exist in the store.
     *
     * @param key  the key
     * @param defn the defn
     * @return the attribute definition store
     */
    AttributeDefinitionStore registerAttributeDefinition(String key, AttributeDefinition defn);

    /**
     * Locate attribute definition by definition name.
     *
     * @param name the name
     * @return the optional
     */
    Optional<AttributeDefinition> locateAttributeDefinitionByName(String name);

    /**
     * Locate attribute definition by name optional.
     *
     * @param <T>   the type parameter
     * @param name  the name
     * @param clazz the clazz
     * @return the optional
     */
    default <T extends AttributeDefinition> Optional<T> locateAttributeDefinitionByName(final String name, final Class<T> clazz) {
        return locateAttributeDefinitionByName(name).map(clazz::cast);
    }

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
     * @param key the name
     * @return the optional
     */
    Optional<AttributeDefinition> locateAttributeDefinition(String key);

    /**
     * Locate attribute definition optional.
     *
     * @param <T>   the type parameter
     * @param key   the name
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
     * Gets attribute definitions by type.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @return the attribute definitions by
     */
    <T extends AttributeDefinition> Stream<T> getAttributeDefinitionsBy(Class<T> type);

    /**
     * Gets attribute values.
     *
     * @param key     the key
     * @param context the context
     * @return the attribute values
     */
    Optional<Pair<AttributeDefinition, List<Object>>> resolveAttributeValues(
        String key, AttributeDefinitionResolutionContext context);

    /**
     * Gets attribute values.
     *
     * @param attributeDefinitions the attribute definitions
     * @param availableAttributes  the available attributes
     * @param principal            the principal
     * @param registeredService    the registered service
     * @param service              the service
     * @return the attribute values
     */
    Map<String, List<Object>> resolveAttributeValues(
        Collection<String> attributeDefinitions,
        Map<String, List<Object>> availableAttributes,
        Principal principal,
        RegisteredService registeredService,
        Service service);

    /**
     * Determine if attribute definition store is empty.
     *
     * @return true/false
     */
    boolean isEmpty();

    /**
     * Store the definitions in a resource.
     *
     * @param resource the resource
     * @return the attribute definition store
     */
    AttributeDefinitionStore store(Resource resource);
}
