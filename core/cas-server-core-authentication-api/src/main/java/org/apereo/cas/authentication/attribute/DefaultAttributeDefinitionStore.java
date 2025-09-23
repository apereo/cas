package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.hjson.JsonValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This is {@link DefaultAttributeDefinitionStore}.
 *
 * @author Misagh Moayyed
 * @author Travis Schmidt
 * @since 6.2.0
 */
@Slf4j
@EqualsAndHashCode(of = "attributeDefinitions")
@ToString(of = "attributeDefinitions")
public class DefaultAttributeDefinitionStore implements AttributeDefinitionStore, DisposableBean, AutoCloseable {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final Map<String, AttributeDefinition> attributeDefinitions = Collections.synchronizedMap(new ConcurrentHashMap<>());

    private FileWatcherService storeWatcherService;

    @Setter
    @Getter
    private String scope = StringUtils.EMPTY;

    public DefaultAttributeDefinitionStore(final Resource resource) throws Exception {
        if (ResourceUtils.doesResourceExist(resource)) {
            importStore(resource);
            watchStore(resource);
        }
    }

    public DefaultAttributeDefinitionStore(final AttributeDefinition... definitions) {
        Arrays.stream(definitions).forEach(this::registerAttributeDefinition);
    }

    /**
     * Register attribute definitions.
     *
     * @param entries the entries
     */
    public void registerAttributeDefinitions(final Map<String, AttributeDefinition> entries) {
        entries.forEach(this::registerAttributeDefinition);
    }

    private static String getAttributeDefinitionKey(final String key, final AttributeDefinition definition) {
        if (StringUtils.isNotBlank(definition.getKey()) && !Strings.CI.equals(definition.getKey(), key)) {
            LOGGER.warn("Attribute definition contains a key property [{}] that differs from its registering key [{}]. "
                + "This is likely due to misconfiguration of the attribute definition, and CAS will use the key property [{}] "
                + "to register the attribute definition in the attribute store", definition.getKey(), key, definition.getKey());
            return definition.getKey();
        }
        return key;
    }

    @Override
    @CanIgnoreReturnValue
    public AttributeDefinitionStore registerAttributeDefinition(final AttributeDefinition definition) {
        return registerAttributeDefinition(definition.getKey(), definition);
    }

    @Override
    @CanIgnoreReturnValue
    public AttributeDefinitionStore registerAttributeDefinition(final String key, final AttributeDefinition definition) {
        LOGGER.trace("Registering attribute definition [{}] by key [{}]", definition, key);
        val keyToUse = getAttributeDefinitionKey(key, definition);
        attributeDefinitions.put(keyToUse, definition);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public AttributeDefinitionStore removeAttributeDefinition(final String key) {
        LOGGER.debug("Removing attribute definition by key [{}]", key);

        if (this.attributeDefinitions.containsKey(key)) {
            val definition = this.attributeDefinitions.remove(key);
            LOGGER.debug("Attribute definition [{}] has been removed from the definition store", definition);
        } else {
            LOGGER.debug("Attribute definition with the registered key [{}] was not found and the store was not altered", key);
        }
        return this;
    }

    @Override
    public Optional<AttributeDefinition> locateAttributeDefinitionByName(final String name) {
        return attributeDefinitions
            .values()
            .stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getName()))
            .filter(entry -> entry.getName().equalsIgnoreCase(name))
            .findFirst();
    }

    @Override
    public Optional<AttributeDefinition> locateAttributeDefinition(final String key) {
        LOGGER.trace("Locating attribute definition for [{}]", key);
        return Optional.ofNullable(attributeDefinitions.get(key));
    }

    @Override
    public <T extends AttributeDefinition> Optional<T> locateAttributeDefinition(final String key, final Class<T> clazz) {
        LOGGER.trace("Locating attribute definition for [{}]", key);
        val attributeDefinition = attributeDefinitions.get(key);
        if (attributeDefinition != null && clazz.isAssignableFrom(attributeDefinition.getClass())) {
            return Optional.of((T) attributeDefinition);
        }
        return Optional.empty();
    }

    @Override
    public <T extends AttributeDefinition> Optional<T> locateAttributeDefinition(final Predicate<AttributeDefinition> predicate) {
        return attributeDefinitions.values()
            .stream()
            .filter(predicate)
            .map(definition -> (T) definition)
            .findFirst();
    }

    @Override
    public Collection<AttributeDefinition> getAttributeDefinitions() {
        return attributeDefinitions.values();
    }

    @Override
    public <T extends AttributeDefinition> Stream<T> getAttributeDefinitionsBy(final Class<T> type) {
        return attributeDefinitions
            .values()
            .stream()
            .filter(definition -> type.isAssignableFrom(definition.getClass()))
            .map(type::cast);
    }

    @Override
    public Map<String, List<Object>> resolveAttributeValues(final Collection<String> attributeDefinitions,
                                                            final Map<String, List<Object>> availableAttributes,
                                                            final Principal principal,
                                                            final RegisteredService registeredService,
                                                            final Service service) {
        val finalAttributes = new LinkedHashMap<String, List<Object>>(attributeDefinitions.size());
        attributeDefinitions.forEach(entry -> locateAttributeDefinition(entry).ifPresentOrElse(definition -> {
            val attributeValues = determineValuesForAttributeDefinition(availableAttributes, entry, definition);
            LOGGER.trace("Resolving attribute [{}] from attribute definition store with values [{}]", entry, attributeValues);
            val attributeDefinitionResolutionContext = AttributeDefinitionResolutionContext.builder()
                .attributeValues(attributeValues)
                .principal(principal)
                .registeredService(registeredService)
                .service(service)
                .attributes(availableAttributes)
                .scope(this.scope)
                .build();
            val result = resolveAttributeValues(entry, attributeDefinitionResolutionContext);
            if (result.isPresent()) {
                val resolvedValues = result.get().getValue();
                if (resolvedValues.isEmpty()) {
                    LOGGER.debug("Unable to produce or determine attributes values for attribute definition [{}]", definition);
                } else {
                    LOGGER.trace("Resolving attribute [{}] based on attribute definition [{}]", entry, definition);
                    val attributeKeys = org.springframework.util.StringUtils.commaDelimitedListToSet(
                        StringUtils.defaultIfBlank(definition.getName(), entry));

                    attributeKeys.forEach(key -> {
                        LOGGER.trace("Determined attribute name to be [{}] with values [{}]", key, resolvedValues);
                        finalAttributes.put(key, resolvedValues);
                    });
                }
            }
        }, () -> {
            if (availableAttributes.containsKey(entry)) {
                LOGGER.trace("Using already-resolved attribute name/value, as no attribute definition was found for [{}]", entry);
                finalAttributes.put(entry, availableAttributes.get(entry));
            }
        }));
        LOGGER.trace("Final collection of attributes resolved from attribute definition store is [{}]", finalAttributes);
        return finalAttributes;
    }

    @Override
    public Optional<Pair<AttributeDefinition, List<Object>>> resolveAttributeValues(
        final String key,
        final AttributeDefinitionResolutionContext context) {
        val result = locateAttributeDefinition(key);
        return result.flatMap(definition -> FunctionUtils.doUnchecked(() -> {
            val currentValues = definition.resolveAttributeValues(context.withScope(this.scope));
            return Optional.of(Pair.of(definition, currentValues));
        }));
    }

    @Override
    public boolean isEmpty() {
        return attributeDefinitions.isEmpty();
    }

    @Override
    @CanIgnoreReturnValue
    public AttributeDefinitionStore store(final Resource resource) {
        return FunctionUtils.doUnchecked(() -> {
            val json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.attributeDefinitions);
            LOGGER.trace("Storing attribute definitions as [{}] to [{}]", json, resource);
            try (val writer = Files.newBufferedWriter(resource.getFile().toPath(), StandardCharsets.UTF_8)) {
                writer.write(json);
                writer.flush();
            }
            return this;
        });
    }
    

    /**
     * Import store.
     *
     * @param resource the resource
     */
    public void importStore(final Resource resource) {
        try {
            val map = from(resource);
            map.forEach(this::registerAttributeDefinition);
        } finally {
            LOGGER.debug("Loaded [{}] attribute definition(s).", attributeDefinitions.size());
        }
    }

    @Override
    public void close() {
        if (this.storeWatcherService != null) {
            this.storeWatcherService.close();
        }
    }

    @Override
    public void destroy() {
        close();
    }

    private static List<Object> determineValuesForAttributeDefinition(final Map<String, List<Object>> attributes,
                                                                      final String entry,
                                                                      final AttributeDefinition definition) {
        val attributeKey = StringUtils.defaultIfBlank(definition.getAttribute(), entry);
        if (attributes.containsKey(attributeKey)) {
            return attributes.get(attributeKey);
        }
        return new ArrayList<>();
    }

    /**
     * Watch store.
     *
     * @param resource the resource
     * @throws Exception the exception
     */
    public void watchStore(final Resource resource) throws Exception {
        if (ResourceUtils.isFile(resource)) {
            this.storeWatcherService = new FileWatcherService(resource.getFile(),
                file -> importStore(new FileSystemResource(file)));
            this.storeWatcherService.start(getClass().getSimpleName());
        }
    }

    /**
     * From resource.
     *
     * @param resource the resource
     * @return the map
     */
    public static Map<String, AttributeDefinition> from(final Resource resource) {
        return FunctionUtils.doIfNotNull(resource,
            () -> {
                try (val is = resource.getInputStream()) {
                    LOGGER.trace("Loading attribute definitions from [{}]", resource);
                    val json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    LOGGER.trace("Loaded attribute definitions [{}] from [{}]", json, resource);
                    return MAPPER.readValue(JsonValue.readHjson(json).toString(), new TypeReference<>() {
                    });
                }
            }, Map::<String, AttributeDefinition>of).get();
    }
}
