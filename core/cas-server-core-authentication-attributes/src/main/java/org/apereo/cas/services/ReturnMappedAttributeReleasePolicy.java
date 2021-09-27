package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledGroovyScript;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ReturnMappedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -6249488544306639050L;

    private Map<String, Object> allowedAttributes = new TreeMap<>();

    @JsonCreator
    public ReturnMappedAttributeReleasePolicy(@JsonProperty("allowedAttributes") final Map<String, Object> attributes) {
        this.allowedAttributes = attributes;
    }

    private static void mapSingleAttributeDefinition(final String attributeName,
                                                     final String mappedAttributeName,
                                                     final Object attributeValue,
                                                     final Map<String, List<Object>> resolvedAttributes,
                                                     final Map<String, List<Object>> attributesToRelease) {
        val matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(mappedAttributeName);
        val matcherFile = ScriptingUtils.getMatcherForExternalGroovyScript(mappedAttributeName);

        if (matcherInline.find()) {
            val inlineGroovy = matcherInline.group(1);
            fetchAttributeValueAsInlineGroovyScript(attributeName, resolvedAttributes, attributesToRelease, inlineGroovy);
        } else if (matcherFile.find()) {
            val file = matcherFile.group();
            fetchAttributeValueFromExternalGroovyScript(attributeName, resolvedAttributes, attributesToRelease, file);
        } else {
            mapSimpleSingleAttributeDefinition(attributeName, mappedAttributeName,
                attributeValue, attributesToRelease, resolvedAttributes);
        }
    }

    private static void fetchAttributeValueFromExternalGroovyScript(final String attributeName,
                                                                    final Map<String, List<Object>> resolvedAttributes,
                                                                    final Map<String, List<Object>> attributesToRelease,
                                                                    final String file) {

        ApplicationContextProvider.getScriptResourceCacheManager()
            .ifPresentOrElse(cacheMgr -> {
                val script = cacheMgr.resolveScriptableResource(file, attributeName, file);
                if (script != null) {
                    fetchAttributeValueFromScript(script, attributeName, resolvedAttributes, attributesToRelease);
                }
            },
                () -> {
                    throw new RuntimeException("No groovy script cache manager is available to execute attribute mappings");
                });
    }

    private static void fetchAttributeValueAsInlineGroovyScript(final String attributeName,
                                                                final Map<String, List<Object>> resolvedAttributes,
                                                                final Map<String, List<Object>> attributesToRelease,
                                                                final String inlineGroovy) {
        ApplicationContextProvider.getScriptResourceCacheManager()
            .ifPresentOrElse(cacheMgr -> {
                val script = cacheMgr.resolveScriptableResource(inlineGroovy, attributeName, inlineGroovy);
                fetchAttributeValueFromScript(script, attributeName, resolvedAttributes, attributesToRelease);
            },
                () -> {
                    throw new RuntimeException("No groovy script cache manager is available to execute attribute mappings");
                });
    }

    private static void mapSimpleSingleAttributeDefinition(final String attributeName,
                                                           final String mappedAttributeName,
                                                           final Object attributeValue,
                                                           final Map<String, List<Object>> attributesToRelease,
                                                           final Map<String, List<Object>> resolvedAttributes) {
        if (attributeValue != null) {
            LOGGER.debug("Found attribute [{}] in the list of allowed attributes, mapped to the name [{}]",
                attributeName, mappedAttributeName);
            val values = CollectionUtils.toCollection(attributeValue, ArrayList.class);
            attributesToRelease.put(mappedAttributeName, values);
        } else if (resolvedAttributes.containsKey(mappedAttributeName)) {
            val mappedValue = resolvedAttributes.get(mappedAttributeName);
            LOGGER.debug("Reusing existing already-remapped attribute [{}] with value [{}]", mappedAttributeName, mappedValue);
            attributesToRelease.put(mappedAttributeName, mappedValue);
        } else {
            LOGGER.warn("Could not find value for mapped attribute [{}] that is based off of [{}] in the allowed attributes list. "
                    + "Ensure the original attribute [{}] is retrieved and contains at least a single value. Attribute [{}] "
                    + "will and can not be released without the presence of a value.", mappedAttributeName, attributeName,
                attributeName, mappedAttributeName);
        }
    }

    private static void fetchAttributeValueFromScript(final ExecutableCompiledGroovyScript script,
                                                      final String attributeName,
                                                      final Map<String, List<Object>> resolvedAttributes,
                                                      final Map<String, List<Object>> attributesToRelease) {
        val args = CollectionUtils.wrap("attributes", resolvedAttributes, "logger", LOGGER);
        script.setBinding(args);
        val result = script.execute(args.values().toArray(), Object.class, false);
        if (result != null) {
            LOGGER.debug("Mapped attribute [{}] to [{}] from script", attributeName, result);
            attributesToRelease.put(attributeName, CollectionUtils.wrapList(result));
        } else {
            LOGGER.warn("Groovy-scripted attribute returned no value for [{}]", attributeName);
        }
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
    public Map<String, List<Object>> getAttributesInternal(final Principal principal,
                                                           final Map<String, List<Object>> attrs,
                                                           final RegisteredService registeredService,
                                                           final Service selectedService) {
        return authorizeReleaseOfAllowedAttributes(principal, attrs, registeredService, selectedService);
    }

    @Override
    public List<String> determineRequestedAttributeDefinitions(final Principal principal,
                                                               final RegisteredService registeredService,
                                                               final Service selectedService) {
        return new ArrayList<>(getAllowedAttributes().keySet());
    }

    /**
     * Authorize release of allowed attributes map.
     * Map each entry in the allowed list into an array first
     * by the original key, value and the original entry itself.
     * Then process the array to populate the map for allowed attributes.
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
        getAllowedAttributes().forEach((attributeName, value) -> {
            val mappedAttributes = CollectionUtils.wrap(value);
            LOGGER.trace("Attempting to map allowed attribute name [{}]", attributeName);
            val attributeValue = resolvedAttributes.get(attributeName);
            mappedAttributes.forEach(mapped -> {
                val mappedAttributeName = mapped.toString();
                LOGGER.debug("Mapping attribute [{}] to [{}] with value [{}]",
                    attributeName, mappedAttributeName, attributeValue);
                mapSingleAttributeDefinition(attributeName, mappedAttributeName,
                    attributeValue, resolvedAttributes, attributesToRelease);
            });
        });
        return attributesToRelease;
    }
}
