package org.apereo.cas.authentication.attribute;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.io.FileWatcherService;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hjson.JsonValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;

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
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .findAndRegisterModules();

    private final Map<String, AttributeDefinition> attributeDefinitions = new TreeMap<>();

    private FileWatcherService storeWatcherService;

    @Setter
    @Getter
    private String scope = StringUtils.EMPTY;

    public DefaultAttributeDefinitionStore(final Resource resource) throws Exception {
        if (ResourceUtils.doesResourceExist(resource)) {
            loadAttributeDefinitionsFromInputStream(resource);

            if (ResourceUtils.isFile(resource)) {
                this.storeWatcherService = new FileWatcherService(resource.getFile(), file -> {
                    try {
                        loadAttributeDefinitionsFromInputStream(new FileSystemResource(file));
                    } catch (final Exception e) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.error(e.getMessage(), e);
                        } else {
                            LOGGER.error(e.getMessage());
                        }
                    }
                });
                this.storeWatcherService.start(getClass().getSimpleName());
            }
        }
    }

    public DefaultAttributeDefinitionStore(final AttributeDefinition... defns) {
        Arrays.stream(defns).forEach(this::registerAttributeDefinition);
    }

    private void loadAttributeDefinitionsFromInputStream(final Resource resource) throws IOException {
        try {
            LOGGER.trace("Loading attribute definitions from [{}]", resource);
            val json = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            LOGGER.trace("Loaded attribute definitions [{}] from [{}]", json, resource);
            val map = MAPPER.readValue(JsonValue.readHjson(json).toString(), new TypeReference<Map<String, AttributeDefinition>>() {
            });
            map.forEach(this::registerAttributeDefinition);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public AttributeDefinitionStore registerAttributeDefinition(final AttributeDefinition defn) {
        return registerAttributeDefinition(defn.getKey(), defn);
    }

    @Override
    public AttributeDefinitionStore registerAttributeDefinition(final String key, final AttributeDefinition defn) {
        LOGGER.trace("Registering attribute definition [{}] by key [{}]", defn, key);

        if (StringUtils.isNotBlank(defn.getKey()) && !StringUtils.equalsIgnoreCase(defn.getKey(), key)) {
            LOGGER.warn("Attribute definition contains a key property [{}] that differs from its registering key [{}]. "
                + "This is likely due to misconfiguration of the attribute definition, and CAS will use the key property [{}] "
                + "to register the attribute definition in the attribute store", defn.getKey(), key, defn.getKey());
            attributeDefinitions.put(defn.getKey(), defn);
        } else {
            attributeDefinitions.put(key, defn);
        }
        return this;
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
            .filter(predicate::test)
            .map(defn -> (T) defn)
            .findFirst();
    }

    @Override
    public Collection<AttributeDefinition> getAttributeDefinitions() {
        return attributeDefinitions.values();
    }

    @Override
    public Optional<Pair<AttributeDefinition, List<Object>>> resolveAttributeValues(final String key, final List<Object> attributeValues,
                                                                                    final RegisteredService registeredService) {
        val result = locateAttributeDefinition(key);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        val definition = result.get();
        val currentValues = definition.resolveAttributeValues(attributeValues, this.scope, registeredService);
        return Optional.of(Pair.of(definition, currentValues));
    }

    @Override
    public boolean isEmpty() {
        return attributeDefinitions.isEmpty();
    }

    @SneakyThrows
    public AttributeDefinitionStore to(final File resource) {
        val json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.attributeDefinitions);
        LOGGER.trace("Storing attribute definitions as [{}] to [{}]", json, resource);
        try (val writer = Files.newBufferedWriter(resource.toPath(), StandardCharsets.UTF_8)) {
            writer.write(json);
            writer.flush();
        }
        return this;
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
}
