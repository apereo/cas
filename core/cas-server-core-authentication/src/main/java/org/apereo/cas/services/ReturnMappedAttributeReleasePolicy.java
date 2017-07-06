package org.apereo.cas.services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Triple;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.RegexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Return a collection of allowed attributes for the principal, but additionally,
 * offers the ability to rename attributes on a per-service level.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class ReturnMappedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -6249488544306639050L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnMappedAttributeReleasePolicy.class);

    private static final Pattern INLINE_GROOVY_PATTERN = RegexUtils.createPattern("groovy\\s*\\{(.+)\\}");
    private static final Pattern FILE_GROOVY_PATTERN = RegexUtils.createPattern("file:(.+\\.groovy)");

    private Map<String, Object> allowedAttributes;

    /**
     * Instantiates a new Return mapped attribute release policy.
     */
    public ReturnMappedAttributeReleasePolicy() {
        this(new TreeMap<>());
    }

    /**
     * Instantiates a new Return mapped attribute release policy.
     *
     * @param allowedAttributes the allowed attributes
     */
    public ReturnMappedAttributeReleasePolicy(final Map allowedAttributes) {
        this.allowedAttributes = allowedAttributes;
    }

    /**
     * Sets the allowed attributes.
     *
     * @param allowed the allowed attributes.
     */
    public void setAllowedAttributes(final Map<String, Object> allowed) {
        this.allowedAttributes = allowed;
    }

    /**
     * Gets the allowed attributes.
     *
     * @return the allowed attributes
     */
    public Map<String, Object> getAllowedAttributes() {
        return new TreeMap<>(this.allowedAttributes);
    }

    @Override
    protected Map<String, Object> getAttributesInternal(final Principal principal,
                                                        final Map<String, Object> attrs,
                                                        final RegisteredService service) {
        final Map<String, Object> resolvedAttributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attrs);

        final Map<String, Object> attributesToRelease = new HashMap<>(resolvedAttributes.size());

        /*
         * Map each entry in the allowed list into an array first
         * by the original key, value and the original entry itself.
         * Then process the array to populate the map for allowed attributes
         */
        this.allowedAttributes.entrySet()
                .stream()
                .map(entry -> {
                    final String key = entry.getKey();
                    final Collection<String> mappedAtributes = (Collection<String>) entry.getValue();
                    LOGGER.debug("Attempting to map allowed attribute name [{}]", key);
                    return Triple.of(key, resolvedAttributes.get(key), mappedAtributes);
                })
                .forEach(entry -> {
                    final Collection<String> mappedAttributes = entry.getRight();
                    mappedAttributes.forEach(mapped -> mapSingleAttributeDefinition(entry.getLeft(), mapped,
                            entry.getMiddle(), resolvedAttributes, attributesToRelease));
                });
        return attributesToRelease;
    }

    private static void mapSingleAttributeDefinition(final String attributeName,
                                                     final String mappedAttributeName,
                                                     final Object attributeValue,
                                                     final Map<String, Object> resolvedAttributes,
                                                     final Map<String, Object> attributesToRelease) {
        final Matcher matcherInline = INLINE_GROOVY_PATTERN.matcher(mappedAttributeName);
        final Matcher matcherFile = FILE_GROOVY_PATTERN.matcher(mappedAttributeName);

        if (matcherInline.find()) {
            processInlineGroovyAttribute(resolvedAttributes, attributesToRelease, matcherInline, attributeName);
        } else if (matcherFile.find()) {
            processFileBasedGroovyAttributes(resolvedAttributes, attributesToRelease, matcherFile, attributeName);
        } else {
            if (attributeValue != null) {
                LOGGER.debug("Found attribute [{}] in the list of allowed attributes, mapped to the name [{}]",
                        attributeName, mappedAttributeName);
                attributesToRelease.put(mappedAttributeName, attributeValue);
            } else {
                LOGGER.warn("Could not find value for mapped attribute [{}] that is based off of [{}] in the allowed attributes list",
                        mappedAttributeName, attributeName);
            }
        }
    }

    private static void processFileBasedGroovyAttributes(final Map<String, Object> resolvedAttributes,
                                                         final Map<String, Object> attributesToRelease,
                                                         final Matcher matcherFile, final String key) {
        try {
            LOGGER.debug("Found groovy script to execute for attribute mapping [{}]", key);
            final String script = FileUtils.readFileToString(new File(matcherFile.group(1)), StandardCharsets.UTF_8);
            final Object result = getGroovyAttributeValue(script, resolvedAttributes);
            if (result != null) {
                LOGGER.debug("Mapped attribute [{}] to [{}] from script", key, result);
                attributesToRelease.put(key, result);
            } else {
                LOGGER.warn("Groovy-scripted attribute returned no value for [{}]", key);
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static void processInlineGroovyAttribute(final Map<String, Object> resolvedAttributes,
                                                     final Map<String, Object> attributesToRelease,
                                                     final Matcher matcherInline,
                                                     final String attributeName) {
        LOGGER.debug("Found inline groovy script to execute for attribute mapping [{}]", attributeName);
        final Object result = getGroovyAttributeValue(matcherInline.group(1), resolvedAttributes);
        if (result != null) {
            LOGGER.debug("Mapped attribute [{}] to [{}] from script", attributeName, result);
            attributesToRelease.put(attributeName, result);
        } else {
            LOGGER.warn("Groovy-scripted attribute returned no value for [{}]", attributeName);
        }
    }

    private static Object getGroovyAttributeValue(final String groovyScript,
                                                  final Map<String, Object> resolvedAttributes) {
        try {
            final Binding binding = new Binding();
            final GroovyShell shell = new GroovyShell(binding);
            binding.setVariable("attributes", resolvedAttributes);
            binding.setVariable("logger", LOGGER);

            LOGGER.debug("Executing groovy script [{}] with attributes binding of [{}]",
                    StringUtils.abbreviate(groovyScript, groovyScript.length() / 2), resolvedAttributes);
            final Object res = shell.evaluate(groovyScript);
            return res;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final ReturnMappedAttributeReleasePolicy rhs = (ReturnMappedAttributeReleasePolicy) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.allowedAttributes, rhs.allowedAttributes)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.allowedAttributes)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("allowedAttributes", this.allowedAttributes)
                .toString();
    }
}
