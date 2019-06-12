package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;

/**
 * Return a collection of allowed attributes for the principal, but additionally,
 * offers the ability to rename attributes on a per-service level.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@ToString(callSuper = true)
@Setter
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReturnMappedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -6249488544306639050L;

    private Map<String, Object> allowedAttributes = new TreeMap<>();

    private static void mapSingleAttributeDefinition(final String attributeName, final String mappedAttributeName,
                                                     final Object attributeValue, final Map<String, List<Object>> resolvedAttributes,
                                                     final Map<String, List<Object>> attributesToRelease) {
        val matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(mappedAttributeName);
        val matcherFile = ScriptingUtils.getMatcherForExternalGroovyScript(mappedAttributeName);
        if (matcherInline.find()) {
            LOGGER.debug("Mapped attribute [{}] is an inlined groovy script", mappedAttributeName);
            processInlineGroovyAttribute(resolvedAttributes, attributesToRelease, matcherInline, attributeName);
        } else if (matcherFile.find()) {
            LOGGER.debug("Mapped attribute [{}] is an external groovy script", mappedAttributeName);
            processFileBasedGroovyAttributes(resolvedAttributes, attributesToRelease, matcherFile, attributeName);
        } else {
            if (attributeValue != null) {
                LOGGER.debug("Found attribute [{}] in the list of allowed attributes, mapped to the name [{}]",
                    attributeName, mappedAttributeName);
                attributesToRelease.put(mappedAttributeName, CollectionUtils.toCollection(attributeValue, ArrayList.class));
            } else {
                LOGGER.warn("Could not find value for mapped attribute [{}] that is based off of [{}] in the allowed attributes list. "
                        + "Ensure the original attribute [{}] is retrieved and contains at least a single value. Attribute [{}] "
                        + "will and can not be released without the presence of a value.", mappedAttributeName, attributeName,
                    attributeName, mappedAttributeName);
            }
        }
    }

    private static void processFileBasedGroovyAttributes(final Map<String, List<Object>> resolvedAttributes,
                                                         final Map<String, List<Object>> attributesToRelease,
                                                         final Matcher matcherFile, final String key) {
        try {
            LOGGER.trace("Found groovy script to execute for attribute mapping [{}]", key);
            val file = new File(matcherFile.group(2));
            val script = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            val result = getGroovyAttributeValue(script, resolvedAttributes);
            if (result != null) {
                LOGGER.debug("Mapped attribute [{}] to [{}] from script", key, result);
                val converted = CollectionUtils.toCollection(result, ArrayList.class);
                attributesToRelease.put(key, converted);
            } else {
                LOGGER.warn("Groovy-scripted attribute returned no value for [{}]", key);
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static void processInlineGroovyAttribute(final Map<String, List<Object>> resolvedAttributes,
                                                     final Map<String, List<Object>> attributesToRelease,
                                                     final Matcher matcherInline, final String attributeName) {
        LOGGER.trace("Found inline groovy script to execute for attribute mapping [{}]", attributeName);
        val result = getGroovyAttributeValue(matcherInline.group(1), resolvedAttributes);
        if (result != null) {
            LOGGER.debug("Mapped attribute [{}] to [{}] from script", attributeName, result);
            attributesToRelease.put(attributeName, CollectionUtils.wrapList(result));
        } else {
            LOGGER.warn("Groovy-scripted attribute returned no value for [{}]", attributeName);
        }
    }

    private static Object getGroovyAttributeValue(final String groovyScript, final Map<String, List<Object>> resolvedAttributes) {
        val args = CollectionUtils.wrap("attributes", resolvedAttributes, "logger", LOGGER);
        return ScriptingUtils.executeGroovyShellScript(groovyScript, args, Object.class);
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
    public Map<String, List<Object>> getAttributesInternal(final Principal principal, final Map<String, List<Object>> attrs,
                                                           final RegisteredService registeredService, final Service selectedService) {
        return authorizeReleaseOfAllowedAttributes(principal, attrs, registeredService, selectedService);
    }

    /**
     * Authorize release of allowed attributes map.
     *
     * @param principal         the principal
     * @param attrs             the attributes
     * @param registeredService the registered service
     * @param selectedService   the selected service
     * @return the map
     */
    protected Map<String, List<Object>> authorizeReleaseOfAllowedAttributes(final Principal principal,
                                                                            final Map<String, List<Object>> attrs,
                                                                            final RegisteredService registeredService,
                                                                            final Service selectedService) {
        val resolvedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attrs);
        val attributesToRelease = new HashMap<String, List<Object>>();
        /*
         * Map each entry in the allowed list into an array first
         * by the original key, value and the original entry itself.
         * Then process the array to populate the map for allowed attributes
         */
        getAllowedAttributes().forEach((attributeName, value) -> {
            val mappedAttributes = CollectionUtils.wrap(value);
            LOGGER.trace("Attempting to map allowed attribute name [{}]", attributeName);
            val attributeValue = resolvedAttributes.get(attributeName);
            mappedAttributes.forEach(mapped -> {
                val mappedAttributeName = mapped.toString();
                LOGGER.debug("Mapping attribute [{}] to [{}] with value [{}]", attributeName, mappedAttributeName, attributeValue);
                mapSingleAttributeDefinition(attributeName, mappedAttributeName,
                    attributeValue, resolvedAttributes, attributesToRelease);
            });
        });
        return attributesToRelease;
    }
}
