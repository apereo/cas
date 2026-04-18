package org.apereo.cas.authentication.attribute;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.core.cache.SimpleCacheProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Expiry;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
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
import org.springframework.core.io.Resource;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link AbstractAttributeDefinitionStore}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
@ToString(of = "attributeDefinitionsCache")
@JsonIgnoreProperties({"attributeDefinitionsCache"})
public abstract class AbstractAttributeDefinitionStore implements AttributeDefinitionStore, DisposableBean, AutoCloseable {
    protected static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @JsonIgnore
    private final Cache<String, AttributeDefinition> attributeDefinitionsCache =
        Beans.newCache(new SimpleCacheProperties(), new AttributeDefinitionExpiry());
    
    @Setter
    @Getter
    private String scope = StringUtils.EMPTY;

    protected AbstractAttributeDefinitionStore(final AttributeDefinition... definitions) {
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
        attributeDefinitionsCache.put(keyToUse, definition);
        return this;
    }

    @Override
    public Optional<AttributeDefinition> locateAttributeDefinitionByName(final String name) {
        return attributeDefinitionsCache
            .asMap()
            .values()
            .stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getName()))
            .filter(entry -> entry.getName().equalsIgnoreCase(name))
            .findFirst();
    }

    @Override
    public Optional<AttributeDefinition> locateAttributeDefinition(final String key) {
        LOGGER.trace("Locating attribute definition for attribute key [{}]", key);
        return Optional.ofNullable(attributeDefinitionsCache.getIfPresent(key));
    }

    @Override
    public <T extends AttributeDefinition> Optional<T> locateAttributeDefinition(final String key, final Class<T> clazz) {
        val attributeDefinition = locateAttributeDefinition(key);
        if (attributeDefinition.isPresent() && clazz.isAssignableFrom(attributeDefinition.get().getClass())) {
            return attributeDefinition.map(clazz::cast);
        }
        return Optional.empty();
    }

    @Override
    public <T extends AttributeDefinition> Optional<T> locateAttributeDefinition(final Predicate<AttributeDefinition> predicate) {
        return attributeDefinitionsCache
            .asMap()
            .values()
            .stream()
            .filter(predicate)
            .map(definition -> (T) definition)
            .findFirst();
    }

    @JsonIgnore
    public Collection<AttributeDefinition> getAttributeDefinitions() {
        return attributeDefinitionsCache.asMap().values();
    }

    @Override
    public <T extends AttributeDefinition> Stream<T> getAttributeDefinitionsBy(final Class<T> type) {
        return attributeDefinitionsCache
            .asMap()
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
        return attributeDefinitionsCache.estimatedSize() <= 0;
    }

    @Override
    public void destroy() throws Exception {
        close();
    }

    @Override
    public void close() {
    }

    @JsonProperty("attributeDefinitions")
    public Map<String, AttributeDefinition> getAttributeDefinitionsMap() {
        return new TreeMap<>(attributeDefinitionsCache.asMap());
    }

    @Override
    @CanIgnoreReturnValue
    public AttributeDefinitionStore removeAttributeDefinition(final String key) {
        LOGGER.debug("Removing attribute definition by key [{}]", key);
        attributeDefinitionsCache.invalidate(key);
        return this;
    }

    @Override
    public int hashCode() {
        return getAttributeDefinitions().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof final AbstractAttributeDefinitionStore other)) {
            return false;
        }
        val givenMap = other.getAttributeDefinitionsMap();
        return getAttributeDefinitionsMap().keySet().containsAll(givenMap.keySet());
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
            LOGGER.debug("Loaded [{}] attribute definition(s).", attributeDefinitionsCache.estimatedSize());
        }
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
                    return MAPPER.readValue(JsonValue.readHjson(json).toString(), TreeMap.class);
                }
            }, Map::<String, AttributeDefinition>of).get();
    }

    private static class AttributeDefinitionExpiry implements Expiry<String, AttributeDefinition> {
        @Override
        public long expireAfterCreate(final String key, final AttributeDefinition value, final long currentTime) {
            return getExpirationTime(value);
        }

        @Override
        public long expireAfterUpdate(final String key, final AttributeDefinition value, final long currentTime, final long currentDuration) {
            return getExpirationTime(value);
        }

        @Override
        public long expireAfterRead(final String key, final AttributeDefinition value, final long currentTime, final long currentDuration) {
            return getExpirationTime(value);
        }

        private static long getExpirationTime(final AttributeDefinition value) {
            if (value.getExpirationTime() != null) {
                val now = ZonedDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.SECONDS);
                val remaining = Duration.between(now, value.getExpirationTime().truncatedTo(ChronoUnit.SECONDS));
                if (remaining.isNegative() || remaining.isZero()) {
                    return 0;
                }
                return remaining.toNanos();
            }
            return Long.MAX_VALUE;
        }

    }
}
