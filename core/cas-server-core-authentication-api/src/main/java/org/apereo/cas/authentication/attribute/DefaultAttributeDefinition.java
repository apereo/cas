package org.apereo.cas.authentication.attribute;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import jakarta.annotation.Nonnull;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultAttributeDefinition}.
 *
 * @author Misagh Moayyed
 * @author Travis Schmidt
 * @since 6.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@EqualsAndHashCode(of = "key")
@Getter
@Setter
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor
@Slf4j
@With
public class DefaultAttributeDefinition implements AttributeDefinition {
    @Serial
    private static final long serialVersionUID = 6898745248727445565L;

    private String key;

    private String name;

    private boolean scoped;

    private boolean encrypted;

    private String attribute;

    private String patternFormat;

    @ExpressionLanguageCapable
    private String script;

    private String canonicalizationMode;

    @Builder.Default
    private Map<String, String> patterns = new LinkedHashMap<>();

    private String flattened;

    private boolean singleValue;

    private String hashingStrategy;

    private static List<Object> formatValuesWithScope(final String scope, final List<Object> currentValues) {
        return currentValues
            .stream()
            .map(value -> String.format("%s@%s", value, scope))
            .collect(Collectors.toList());
    }

    private static List<Object> encryptValues(final List<Object> currentValues, final RegisteredService registeredService) {
        val publicKey = registeredService.getPublicKey();
        if (publicKey == null) {
            LOGGER.error("No public key is defined for service [{}]. No attributes will be released", registeredService);
            return new ArrayList<>();
        }
        val cipher = publicKey.toCipher();
        if (cipher == null) {
            LOGGER.error("Unable to initialize cipher given the public key algorithm [{}]", publicKey.getAlgorithm());
            return new ArrayList<>();
        }

        return currentValues
            .stream()
            .map(Unchecked.function(value -> {
                LOGGER.trace("Encrypting attribute value [{}]", value);
                val result = EncodingUtils.encodeBase64(cipher.doFinal(value.toString().getBytes(StandardCharsets.UTF_8)));
                LOGGER.trace("Encrypted attribute value [{}]", result);
                return result;
            }))
            .collect(Collectors.toList());
    }

    private static List<Object> fetchAttributeValueFromExternalGroovyScript(final String attributeName,
                                                                            final List<Object> currentValues,
                                                                            final String file,
                                                                            final AttributeDefinitionResolutionContext context) throws Throwable {
        val result = ApplicationContextProvider.getScriptResourceCacheManager();
        if (result.isPresent()) {
            val cacheMgr = result.get();
            val script = cacheMgr.resolveScriptableResource(file, attributeName, file);
            if (script != null) {
                return fetchAttributeValueFromScript(script, attributeName, currentValues, context);
            }
        }
        LOGGER.warn("No groovy script cache manager is available to execute attribute mappings");
        return new ArrayList<>();
    }

    private static List<Object> fetchAttributeValueAsInlineGroovyScript(final String attributeName,
                                                                        final List<Object> currentValues,
                                                                        final String inlineGroovy,
                                                                        final AttributeDefinitionResolutionContext context) {
        return ApplicationContextProvider.getScriptResourceCacheManager()
            .map(cacheManager -> FunctionUtils.doUnchecked(() -> {
                val script = cacheManager.resolveScriptableResource(inlineGroovy, attributeName, inlineGroovy);
                return fetchAttributeValueFromScript(script, attributeName, currentValues, context);
            })).orElseGet(() -> {
                LOGGER.warn("No groovy script cache manager is available to execute attribute mappings");
                return new ArrayList<>();
            });
    }

    private static List<Object> fetchAttributeValueFromScript(final ExecutableCompiledScript scriptToExec,
                                                              final String attributeKey,
                                                              final List<Object> currentValues,
                                                              final AttributeDefinitionResolutionContext context) throws Throwable {
        val args = CollectionUtils.<String, Object>wrap("attributeName", Objects.requireNonNull(attributeKey),
            "attributeValues", currentValues, "logger", LOGGER,
            "registeredService", context.getRegisteredService(),
            "attributes", context.getAttributes());
        scriptToExec.setBinding(args);
        return scriptToExec.execute(args.values().toArray(), List.class);
    }

    @Override
    public int compareTo(@Nonnull final AttributeDefinition definition) {
        return Comparator.comparing(AttributeDefinition::getKey).compare(this, definition);
    }
    
    @JsonIgnore
    @Override
    public List<Object> resolveAttributeValues(final AttributeDefinitionResolutionContext context) throws Throwable {
        List<Object> currentValues = new ArrayList<>(context.getAttributeValues());
        if (StringUtils.isNotBlank(getScript())) {
            currentValues = getScriptedAttributeValue(key, currentValues, context);
        }
        if (getPatterns() != null && !getPatterns().isEmpty() && !currentValues.isEmpty()) {
            currentValues = getPatternValuesFor(currentValues, context);
        }
        if (isScoped()) {
            currentValues = formatValuesWithScope(context.getScope(), currentValues);
        }
        if (StringUtils.isNotBlank(getPatternFormat())) {
            currentValues = formatValuesWithPattern(currentValues);
        }
        if (StringUtils.isNotBlank(getHashingStrategy())) {
            currentValues = hashValues(currentValues);
        }
        
        if (isEncrypted()) {
            currentValues = encryptValues(currentValues, context.getRegisteredService());
        }
        if (StringUtils.isNotBlank(this.canonicalizationMode)) {
            val mode = CaseCanonicalizationMode.valueOf(canonicalizationMode.toUpperCase(Locale.ENGLISH));
            currentValues = Objects.requireNonNull(currentValues)
                .stream()
                .map(value -> mode.canonicalize(value.toString()))
                .collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(getFlattened()) && currentValues.size() > 1) {
            val flattenedValue = currentValues.stream().map(Object::toString).collect(Collectors.joining(getFlattened()));
            currentValues.clear();
            currentValues.add(flattenedValue);
        }
        LOGGER.trace("Resolved values [{}] for attribute definition [{}]", currentValues, this);
        return currentValues;
    }

    private List<Object> hashValues(final List<Object> currentValues) {
        return currentValues
            .stream()
            .map(value -> switch (getHashingStrategy().toLowerCase(Locale.ENGLISH)) {
                case "hex" -> EncodingUtils.hexEncode(value.toString());
                case "base64" -> EncodingUtils.encodeBase64(value.toString());
                case "sha", "sha1" -> DigestUtils.sha(value.toString());
                case "sha256" -> DigestUtils.sha256(value.toString());
                case "sha512" -> DigestUtils.sha512(value.toString());
                default -> value;
            })
            .collect(Collectors.toList());
    }

    private List<Object> getPatternValuesFor(final List<Object> currentValues,
                                             final AttributeDefinitionResolutionContext context) {
        return patterns
            .entrySet()
            .stream()
            .map(entry -> {
                val pattern = RegexUtils.createPattern(entry.getKey());
                return currentValues.stream()
                    .filter(value -> RegexUtils.find(pattern, value.toString()))
                    .map(value -> getScriptedPatternedValue(value, entry.getValue(), context))
                    .findFirst()
                    .orElse(StringUtils.EMPTY);
            })
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    }

    private List<Object> formatValuesWithPattern(final List<Object> currentValues) {
        return currentValues
            .stream()
            .map(value -> MessageFormat.format(getPatternFormat(), value))
            .collect(Collectors.toList());
    }

    private List<Object> getScriptedAttributeValue(final String attributeKey,
                                                   final List<Object> currentValues,
                                                   final AttributeDefinitionResolutionContext context) throws Throwable {
        LOGGER.trace("Locating attribute value via script for definition [{}]", this);

        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        
        if (scriptFactory.isInlineScript(getScript())) {
            val input = scriptFactory.getInlineScript(getScript()).orElseThrow();
            return fetchAttributeValueAsInlineGroovyScript(attributeKey, currentValues, input, context);
        }

        val scriptDefinition = SpringExpressionLanguageValueResolver.getInstance().resolve(getScript());
        if (scriptFactory.isExternalScript(scriptDefinition)) {
            val input = scriptFactory.getExternalScript(scriptDefinition).orElseThrow();
            return fetchAttributeValueFromExternalGroovyScript(attributeKey, currentValues, input, context);
        }

        return new ArrayList<>();
    }

    private static String getScriptedPatternedValue(final Object currentValue,
                                                    final String patternedValue,
                                                    final AttributeDefinitionResolutionContext context) {

        val scriptFactory = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();
        if (scriptFactory.isPresent() && scriptFactory.get().isInlineScript(patternedValue)) {
            return ApplicationContextProvider.getScriptResourceCacheManager()
                .map(cacheManager -> FunctionUtils.doUnchecked(() -> {
                    val script = cacheManager.resolveScriptableResource(patternedValue);
                    val args = CollectionUtils.<String, Object>wrap("context", context,
                        "currentValue", currentValue, "logger", LOGGER);
                    script.setBinding(args);
                    return script.execute(args.values().toArray(), String.class);
                }))
                .orElse(patternedValue);
        }
        return patternedValue;
    }
}
