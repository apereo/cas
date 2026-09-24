package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link PatternMatchingAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Slf4j
public class PatternMatchingAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = -2168544657991721919L;

    private static final Pattern PATTERN_TRANSFORM_GROUPS = RegexUtils.createPattern("\\$\\{(\\d+)\\}");

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, Rule> allowedAttributes = new TreeMap<>();

    @JsonCreator
    public PatternMatchingAttributeReleasePolicy(
        @JsonProperty("allowedAttributes") final Map<String, Rule> attributes) {
        this.allowedAttributes = attributes;
    }

    @Override
    public Map<String, List<Object>> getAttributesInternal(final RegisteredServiceAttributeReleasePolicyContext context,
                                                           final Map<String, List<Object>> attributes) {
        val scriptFactory = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();
        return allowedAttributes
            .entrySet()
            .stream()
            .filter(entry -> attributes.containsKey(entry.getKey()))
            .filter(entry -> {
                if (StringUtils.isBlank(entry.getValue().getTransform())) {
                    LOGGER.warn("No transformation rule is defined for attribute [{}]; the rule is ignored.", entry.getKey());
                    return false;
                }
                return true;
            })
            .map(entry -> {
                val rule = entry.getValue();
                return CasRuntimeHintsRegistrar.notInNativeImage() && scriptFactory.isPresent()
                    && scriptFactory.get().isInlineScript(rule.getTransform())
                    ? buildAttributesForScriptedEntry(attributes, entry, context)
                    : buildAttributesForEntry(attributes, entry);
            })
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                CollectionUtils::wrapList,
                LinkedHashMap::new));
    }

    private static Map<String, List<Object>> buildAttributesForScriptedEntry(
        final Map<String, List<Object>> attributes,
        final Map.Entry<String, Rule> entry,
        final RegisteredServiceAttributeReleasePolicyContext context) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        val rule = entry.getValue();
        val valuePattern = RegexUtils.createPattern(rule.getPattern());
        val attributeValues = attributes.get(entry.getKey());

        val inlineScript = rule.getTransform().trim().stripIndent();
        val inlineGroovy = scriptFactory.getInlineScript(inlineScript).orElseThrow();
        val executableScript = ApplicationContextProvider.getScriptResourceCacheManager()
            .map(cacheManager -> cacheManager.resolveScriptableResource(inlineGroovy, inlineGroovy))
            .orElseGet(() -> scriptFactory.fromScript(inlineGroovy));

        return attributeValues
            .stream()
            .map(value -> executeTransformationRule(executableScript, valuePattern, value, attributes, context))
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                CollectionUtils::wrapList,
                LinkedHashMap::new));
    }

    /**
     * The binding is assigned to the script for the current thread only and is consumed by the
     * execution that immediately follows it on that same thread, which is what allows the compiled
     * script to be shared via {@link org.apereo.cas.util.scripting.ScriptResourceCacheManager}.
     * Assigning a binding here and executing elsewhere would silently discard it.
     */
    private static Map<String, List<Object>> executeTransformationRule(
        final ExecutableCompiledScript executableScript,
        final Pattern valuePattern,
        final Object attributeValue,
        final Map<String, List<Object>> attributes,
        final RegisteredServiceAttributeReleasePolicyContext context) {
        val matcher = valuePattern.matcher(attributeValue.toString());
        if (!matcher.find()) {
            return Map.of();
        }
        val args = CollectionUtils.<String, Object>wrap(
            "context", context,
            "attributes", attributes,
            "matched", matcher.group(),
            "logger", LOGGER);
        for (var i = 0; i <= matcher.groupCount(); i++) {
            args.put("matchedGroup" + i, matcher.group(i));
        }
        executableScript.setBinding(args);
        return FunctionUtils.doUnchecked(() -> normalizeTransformationResult(
            executableScript.execute(args.values().toArray(), Map.class), attributeValue));
    }

    /**
     * Transformation rules are free-form scripts and the attribute map they produce is handed to the
     * rest of the release chain, which requires non-blank names mapped to collections of values. A
     * script that returns a single value, a {@code null} value or no usable entry at all must not be
     * able to corrupt that map or fail the release of every other attribute, so each entry is coerced
     * into that shape here and dropped when it cannot be.
     */
    private static Map<String, List<Object>> normalizeTransformationResult(final Map<?, ?> result,
                                                                           final Object attributeValue) {
        if (result == null || result.isEmpty()) {
            return Map.of();
        }
        val normalized = new LinkedHashMap<String, List<Object>>();
        result.forEach((key, value) -> {
            val attributeName = Objects.toString(key, StringUtils.EMPTY);
            val values = CollectionUtils.<Object>wrapList(value);
            if (StringUtils.isNotBlank(attributeName) && !values.isEmpty()) {
                normalized.put(attributeName, values);
            } else {
                LOGGER.warn("Transformation rule produced an unusable entry [{}] with value [{}] for [{}] and is ignored. "
                    + "Rules must return a map of attribute names to values.", key, value, attributeValue);
            }
        });
        return normalized;
    }

    private static Map<String, List<Object>> buildAttributesForEntry(final Map<String, List<Object>> attributes,
                                                                     final Map.Entry<String, Rule> entry) {
        val rule = entry.getValue();
        val transformPattern = PATTERN_TRANSFORM_GROUPS.matcher(rule.getTransform());
        val valuePattern = RegexUtils.createPattern(rule.getPattern());
        val attributeValues = attributes.get(entry.getKey());
        val transformedValues = attributeValues
            .stream()
            .map(value -> {
                var transformedValue = rule.getTransform();
                val matcher = valuePattern.matcher(value.toString());
                if (matcher.find()) {
                    while (transformPattern.find()) {
                        val group = Integer.parseInt(transformPattern.group(1));
                        val target = String.format("${%s}", group);
                        transformedValue = transformedValue.replace(target, matcher.group(group));
                    }
                }
                transformPattern.reset();
                return transformedValue;
            })
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.<Object>toList());
        return Map.of(entry.getKey(), transformedValues);
    }

    @ToString
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @Accessors(chain = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @EqualsAndHashCode
    public static class Rule implements Serializable {
        @Serial
        private static final long serialVersionUID = 3111910879481087570L;

        private String pattern;

        private String transform;
    }
}
