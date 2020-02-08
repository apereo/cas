package org.apereo.cas.authentication.attribute;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledGroovyScript;
import org.apereo.cas.util.scripting.GroovyShellScript;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.Resource;

import javax.persistence.Transient;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
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
    private static final int MAP_SIZE = 20;

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .findAndRegisterModules();

    private final Map<String, AttributeDefinition> attributeDefinitions = new TreeMap<>();

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    private transient Map<String, ExecutableCompiledGroovyScript> attributeScriptCache = new LinkedHashMap<>(0);

    @Setter
    @Getter
    private String scope = StringUtils.EMPTY;

    public DefaultAttributeDefinitionStore(final AttributeDefinition... defns) {
        Arrays.stream(defns).forEach(this::registerAttributeDefinition);
    }

    public DefaultAttributeDefinitionStore(final Collection<AttributeDefinition> defns) {
        defns.forEach(this::registerAttributeDefinition);
    }

    public DefaultAttributeDefinitionStore(final Map<String, AttributeDefinition> attributeDefns) {
        attributeDefns.forEach(this::registerAttributeDefinition);
    }

    @SneakyThrows
    public static DefaultAttributeDefinitionStore from(final Resource resource) {
        LOGGER.trace("Loading attribute definitions from [{}]", resource);
        val json = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.trace("Loaded attribute definitions [{}] from [{}]", json, resource);
        val map = MAPPER.readValue(json, new TypeReference<Map<String, AttributeDefinition>>() {
        });
        return new DefaultAttributeDefinitionStore(map);
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
    public Collection<AttributeDefinition> getAttributeDefinitions() {
        return attributeDefinitions.values();
    }

    @Override
    public Optional<Pair<AttributeDefinition, List<Object>>> resolveAttributeValues(final String key, final List<Object> attributeValues) {
        val result = locateAttributeDefinition(key);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        val definition = result.get();
        
        List<Object> currentValues = new ArrayList<>(attributeValues);
        if (StringUtils.isNotBlank(definition.getScript())) {
            currentValues = getScriptedAttributeValue(key, currentValues, definition);
        }
        if (definition.isScoped()) {
            currentValues = currentValues
                .stream()
                .map(v -> String.format("%s@%s", v, this.scope))
                .collect(Collectors.toCollection(ArrayList::new));
        }

        if (StringUtils.isNotBlank(definition.getPatternFormat())) {
            currentValues = currentValues
                .stream()
                .map(v -> MessageFormat.format(definition.getPatternFormat(), v))
                .collect(Collectors.toCollection(ArrayList::new));
        }

        return Optional.of(Pair.of(definition, currentValues));
    }

    @Override
    public boolean isEmpty() {
        return attributeDefinitions.isEmpty();
    }

    private List<Object> getScriptedAttributeValue(final String attributeKey,
                                                   final List<Object> currentValues,
                                                   final AttributeDefinition definition) {
        if (this.attributeScriptCache == null) {
            this.attributeScriptCache = new LinkedHashMap<>(MAP_SIZE);
        }
        LOGGER.trace("Locating attribute value via script for definition [{}]", definition);
        if (!attributeScriptCache.containsKey(attributeKey)) {
            val matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(definition.getScript());
            val matcherFile = ScriptingUtils.getMatcherForExternalGroovyScript(definition.getScript());
            if (matcherInline.find()) {
                attributeScriptCache.put(attributeKey, new GroovyShellScript(matcherInline.group(1)));
            } else if (matcherFile.find()) {
                try {
                    val scriptPath = SpringExpressionLanguageValueResolver.getInstance().resolve(matcherFile.group());
                    val resource = ResourceUtils.getRawResourceFrom(scriptPath);
                    attributeScriptCache.put(attributeKey, new WatchableGroovyScriptResource(resource));
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        if (!attributeScriptCache.containsKey(attributeKey)) {
            LOGGER.error("Unable to locate scripted attribute definition for attribute [{}]", attributeKey);
            return new ArrayList<>(0);
        }
        val script = attributeScriptCache.get(attributeKey);
        val args = CollectionUtils.<String, Object>wrap("attributeName", attributeKey,
            "attributeValues", currentValues, "logger", LOGGER);
        script.setBinding(args);
        return script.execute(args.values().toArray(), List.class);
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
}
