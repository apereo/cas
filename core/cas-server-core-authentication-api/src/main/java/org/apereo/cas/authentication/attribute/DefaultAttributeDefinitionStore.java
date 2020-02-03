package org.apereo.cas.authentication.attribute;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
public class DefaultAttributeDefinitionStore implements AttributeDefinitionStore {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .findAndRegisterModules();

    private final Map<String, AttributeDefinition> attributeDefinitions = new TreeMap<>();

    @Setter
    private String scope;

    public DefaultAttributeDefinitionStore(final AttributeDefinition... defns) {
        Arrays.stream(defns).forEach(this::registerAttributeDefinition);
    }

    public DefaultAttributeDefinitionStore(final Collection<AttributeDefinition> defns) {
        defns.forEach(this::registerAttributeDefinition);
    }

    @SneakyThrows
    public static DefaultAttributeDefinitionStore from(final Resource resource) {
        LOGGER.trace("Loading attribute definitions from [{}]", resource);
        val json = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.trace("Loaded attribute definitions [{}] from [{}]", json, resource);
        val map = MAPPER.readValue(json, new TypeReference<Map<String, AttributeDefinition>>() {
        });
        return new DefaultAttributeDefinitionStore(map.values());
    }

    @Override
    public AttributeDefinitionStore registerAttributeDefinition(final AttributeDefinition defn) {
        LOGGER.trace("Registering attribute definition [{}]", defn);
        attributeDefinitions.put(defn.getKey(), defn);
        return this;
    }

    @Override
    public Optional<AttributeDefinition> locateAttributeDefinition(final String key) {
        LOGGER.trace("Locating attribute definition for [{}]", key);
        return Optional.ofNullable(attributeDefinitions.get(key));
    }

    @Override
    public Collection<AttributeDefinition> getAttributeDefinitions() {
        return attributeDefinitions.values();
    }

    @Override
    public Optional<List<Object>> getAttributeValues(final String key, final Map<String, List<Object>> attributes) {
        val result = locateAttributeDefinition(key);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        val definition = result.get();
        val attributeKey = StringUtils.defaultIfBlank(definition.getAttribute(), key);
        if (!attributes.containsKey(attributeKey)) {
            return Optional.empty();
        }
        val currentValues = attributes.get(attributeKey);
        val results = currentValues
            .stream()
            .map(value -> {
                var valueInProcess = value;
                if (StringUtils.isNotBlank(definition.getScript())) {
                    valueInProcess = getScriptedAttributeValue(definition);
                }
                if (definition.isScoped()) {
                    valueInProcess = String.format("%s@%s", valueInProcess, this.scope);
                }
                if (StringUtils.isNotBlank(definition.getTemplate())) {
                    valueInProcess = MessageFormat.format(definition.getTemplate(), value);
                }
                return valueInProcess;
            })
            .collect(Collectors.toList());
        return Optional.of(results);
    }

    private static String getScriptedAttributeValue(final AttributeDefinition definition) {
        LOGGER.trace("Locating attribute value via script for definition [{}]", definition);
        return null;
    }

    @SneakyThrows
    public AttributeDefinitionStore to(final File resource) {
        val json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this.attributeDefinitions);
        LOGGER.trace("Storing attribute definitions as [{}] to [{}]", json, resource);
        try (val writer = Files.newBufferedWriter(resource.toPath(), StandardCharsets.UTF_8)) {
            IOUtils.copy(new StringReader(json), writer);
            writer.flush();
        }
        return this;
    }
}
