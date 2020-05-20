package org.apereo.cas.authentication.attribute;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledGroovyScript;
import org.apereo.cas.util.scripting.GroovyShellScript;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
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
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jooq.lambda.Unchecked;

import javax.persistence.Transient;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
@Builder
@NoArgsConstructor
@Slf4j
public class DefaultAttributeDefinition implements AttributeDefinition {
    private static final long serialVersionUID = 6898745248727445565L;

    private static final int MAP_SIZE = 20;

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    @Builder.Default
    private transient Map<String, ExecutableCompiledGroovyScript> attributeScriptCache = new LinkedHashMap<>(0);

    private String key;

    private String name;

    private boolean scoped;

    private boolean encrypted;

    private String attribute;

    private String patternFormat;

    private String script;

    @Override
    public int compareTo(final AttributeDefinition o) {
        return new CompareToBuilder()
            .append(getKey(), o.getKey())
            .build();
    }

    @JsonIgnore
    @Override
    public List<Object> resolveAttributeValues(final List<Object> attributeValues,
                                               final String scope,
                                               final RegisteredService registeredService) {
        List<Object> currentValues = new ArrayList<>(attributeValues);
        if (StringUtils.isNotBlank(getScript())) {
            currentValues = getScriptedAttributeValue(key, currentValues);
        }
        if (isScoped()) {
            currentValues = formatValuesWithScope(scope, currentValues);
        }
        if (StringUtils.isNotBlank(getPatternFormat())) {
            currentValues = formatValuesWithPattern(currentValues);
        }
        if (isEncrypted()) {
            currentValues = encryptValues(currentValues, registeredService);
        }
        LOGGER.trace("Resolved values [{}] for attribute definition [{}]", currentValues, this);
        return currentValues;
    }

    private static List<Object> formatValuesWithScope(final String scope, final List<Object> currentValues) {
        return currentValues
            .stream()
            .map(v -> String.format("%s@%s", v, scope))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<Object> encryptValues(final List<Object> currentValues, final RegisteredService registeredService) {
        val publicKey = registeredService.getPublicKey();
        if (publicKey == null) {
            LOGGER.error("No public key is defined for service [{}]. No attributes will be released", registeredService);
            return new ArrayList<>(0);
        }
        val cipher = publicKey.toCipher();
        if (cipher == null) {
            LOGGER.error("Unable to initialize cipher given the public key algorithm [{}]", publicKey.getAlgorithm());
            return new ArrayList<>(0);
        }

        return currentValues
            .stream()
            .map(Unchecked.function(value -> {
                LOGGER.trace("Encrypting attribute value [{}]", value);
                val result = EncodingUtils.encodeBase64(cipher.doFinal(value.toString().getBytes(StandardCharsets.UTF_8)));
                LOGGER.trace("Encrypted attribute value [{}]", result);
                return result;
            }))
            .collect(Collectors.toCollection(ArrayList::new));
    }
    
    private List<Object> formatValuesWithPattern(final List<Object> currentValues) {
        return currentValues
            .stream()
            .map(v -> MessageFormat.format(getPatternFormat(), v))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @JsonIgnore
    private List<Object> getScriptedAttributeValue(final String attributeKey,
                                                   final List<Object> currentValues) {
        if (this.attributeScriptCache == null) {
            this.attributeScriptCache = new LinkedHashMap<>(MAP_SIZE);
        }
        LOGGER.trace("Locating attribute value via script for definition [{}]", this);
        if (!attributeScriptCache.containsKey(attributeKey)) {
            val matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(getScript());
            val matcherFile = ScriptingUtils.getMatcherForExternalGroovyScript(getScript());
            if (matcherInline.find()) {
                attributeScriptCache.put(attributeKey, new GroovyShellScript(matcherInline.group(1)));
            } else if (matcherFile.find()) {
                try {
                    val scriptPath = SpringExpressionLanguageValueResolver.getInstance().resolve(matcherFile.group());
                    val resource = ResourceUtils.getRawResourceFrom(scriptPath);
                    attributeScriptCache.put(attributeKey, new WatchableGroovyScriptResource(resource));
                } catch (final Exception e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.error(e.getMessage(), e);
                    } else {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
        }
        if (!attributeScriptCache.containsKey(attributeKey)) {
            LOGGER.error("Unable to locate scripted attribute definition for attribute [{}]", attributeKey);
            return new ArrayList<>(0);
        }
        val scriptToExec = attributeScriptCache.get(attributeKey);
        val args = CollectionUtils.<String, Object>wrap("attributeName", attributeKey,
            "attributeValues", currentValues, "logger", LOGGER);
        scriptToExec.setBinding(args);
        return scriptToExec.execute(args.values().toArray(), List.class);
    }
}
