package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledGroovyScript;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.ws.idp.WSFederationClaims;

import com.google.common.collect.Maps;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link WSFederationClaimsReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class WSFederationClaimsReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
    private static final long serialVersionUID = -2814928645221579489L;

    private Map<String, String> allowedAttributes = new LinkedHashMap<>();

    public WSFederationClaimsReleasePolicy() {
        setAllowedAttributes(new LinkedHashMap<>());
    }

    public WSFederationClaimsReleasePolicy(final Map<String, String> allowedAttributes) {
        setAllowedAttributes(allowedAttributes);
    }

    @Override
    public Map<String, List<Object>> getAttributesInternal(final Principal principal, final Map<String, List<Object>> attrs,
                                                           final RegisteredService registeredService, final Service selectedService) {
        val resolvedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attrs);
        val attributesToRelease = Maps.<String, List<Object>>newHashMapWithExpectedSize(resolvedAttributes.size());

        getAllowedAttributes()
            .entrySet()
            .stream()
            .filter(entry -> WSFederationClaims.contains(entry.getKey().toUpperCase()))
            .forEach(entry -> {
                val claimName = entry.getKey();
                val attributeValue = resolvedAttributes.get(entry.getValue());
                val claim = WSFederationClaims.valueOf(claimName.toUpperCase());
                if (resolvedAttributes.containsKey(claim.getUri())) {
                    attributesToRelease.put(claim.getUri(), resolvedAttributes.get(claim.getUri()));
                } else {
                    LOGGER.trace("Evaluating claim [{}] mapped to attribute value [{}]", claim.getUri(), attributeValue);
                    mapSingleAttributeDefinition(claim.getUri(), entry.getValue(),
                        attributeValue, resolvedAttributes, attributesToRelease);
                }
            });
        return attributesToRelease;
    }

    private static void mapSimpleSingleAttributeDefinition(final String attributeName,
                                                           final String mappedAttributeName,
                                                           final List<Object> attributeValue,
                                                           final Map<String, List<Object>> attributesToRelease) {
        if (attributeValue != null) {
            LOGGER.debug("Found attribute [{}] in the list of allowed attributes, mapped to the name [{}]",
                attributeName, mappedAttributeName);
            val values = CollectionUtils.toCollection(attributeValue, ArrayList.class);
            attributesToRelease.put(attributeName, values);
        } else {
            LOGGER.warn("Could not find value for mapped attribute [{}] that is based off of [{}] in the allowed attributes list. "
                    + "Ensure the original attribute [{}] is retrieved and contains at least a single value. Attribute [{}] "
                    + "will and can not be released without the presence of a value.", mappedAttributeName, attributeName,
                attributeName, mappedAttributeName);
        }
    }

    private static void mapSingleAttributeDefinition(final String attributeName,
                                                     final String mappedAttributeName,
                                                     final List<Object> attributeValue,
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
            mapSimpleSingleAttributeDefinition(attributeName, mappedAttributeName, attributeValue, attributesToRelease);
        }
    }

    private static void fetchAttributeValueFromExternalGroovyScript(final String attributeName,
                                                                    final Map<String, List<Object>> resolvedAttributes,
                                                                    final Map<String, List<Object>> attributesToRelease,
                                                                    final String file) {

        ApplicationContextProvider.getScriptResourceCacheManager().ifPresentOrElse(cacheMgr -> {
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

    private static void fetchAttributeValueFromScript(final ExecutableCompiledGroovyScript script,
                                                      final String attributeName,
                                                      final Map<String, List<Object>> resolvedAttributes,
                                                      final Map<String, List<Object>> attributesToRelease) {
        val args = CollectionUtils.wrap("attributes", resolvedAttributes, "logger", LOGGER);
        script.setBinding(args);
        val result = script.execute(args.values().toArray(), Object.class);
        if (result != null) {
            LOGGER.debug("Mapped attribute [{}] to [{}] from script", attributeName, result);
            attributesToRelease.put(attributeName, CollectionUtils.wrapList(result));
        } else {
            LOGGER.warn("Groovy-scripted attribute returned no value for [{}]", attributeName);
        }
    }

    @Override
    public List<String> determineRequestedAttributeDefinitions(final Principal principal,
                                                               final RegisteredService registeredService,
                                                               final Service selectedService) {
        return new ArrayList<>(getAllowedAttributes().keySet());
    }
}
