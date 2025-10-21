package org.apereo.cas.ws.idp.services;

import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.ws.idp.WSFederationClaims;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
@NoArgsConstructor
public class WSFederationClaimsReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
    @Serial
    private static final long serialVersionUID = -2814928645221579489L;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, String> allowedAttributes = new LinkedHashMap<>();

    public WSFederationClaimsReleasePolicy(final Map<String, String> allowedAttributes) {
        setAllowedAttributes(allowedAttributes);
    }

    @Override
    public Map<String, List<Object>> getAttributesInternal(final RegisteredServiceAttributeReleasePolicyContext context,
                                                           final Map<String, List<Object>> attrs) {
        val resolvedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attrs);
        val attributesToRelease = new HashMap<String, List<Object>>(resolvedAttributes.size());

        getAllowedAttributes()
            .entrySet()
            .stream()
            .filter(entry -> WSFederationClaims.contains(entry.getKey().toUpperCase(Locale.ENGLISH)))
            .forEach(entry -> {
                val claimName = entry.getKey();
                val attributeValue = resolvedAttributes.get(entry.getValue());
                val claim = WSFederationClaims.valueOf(claimName.toUpperCase(Locale.ENGLISH));
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
        val scriptFactoryInstance = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();

        if (scriptFactoryInstance.isPresent()) {
            val scriptFactory = scriptFactoryInstance.get();
            
            if (scriptFactory.isInlineScript(mappedAttributeName)) {
                val inlineGroovy = scriptFactory.getInlineScript(mappedAttributeName).orElseThrow();
                fetchAttributeValueAsInlineGroovyScript(attributeName, resolvedAttributes, attributesToRelease, inlineGroovy);
            } else if (scriptFactory.isExternalScript(mappedAttributeName)) {
                val file = scriptFactory.getExternalScript(mappedAttributeName).orElseThrow();
                fetchAttributeValueFromExternalGroovyScript(attributeName, resolvedAttributes, attributesToRelease, file);
            } else {
                mapSimpleSingleAttributeDefinition(attributeName, mappedAttributeName, attributeValue, attributesToRelease);
            }
        }
        mapSimpleSingleAttributeDefinition(attributeName, mappedAttributeName, attributeValue, attributesToRelease);
    }

    private static void fetchAttributeValueFromExternalGroovyScript(final String attributeName,
                                                                    final Map<String, List<Object>> resolvedAttributes,
                                                                    final Map<String, List<Object>> attributesToRelease,
                                                                    final String file) {

        ApplicationContextProvider.getScriptResourceCacheManager().ifPresentOrElse(
            cacheMgr -> {
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
            .ifPresentOrElse(
                cacheMgr -> {
                    val script = cacheMgr.resolveScriptableResource(inlineGroovy, attributeName, inlineGroovy);
                    fetchAttributeValueFromScript(script, attributeName, resolvedAttributes, attributesToRelease);
                },
                () -> {
                    throw new RuntimeException("No groovy script cache manager is available to execute attribute mappings");
                });
    }

    private static void fetchAttributeValueFromScript(final ExecutableCompiledScript script,
                                                      final String attributeName,
                                                      final Map<String, List<Object>> resolvedAttributes,
                                                      final Map<String, List<Object>> attributesToRelease) {
        FunctionUtils.doUnchecked(_ -> {
            val args = CollectionUtils.wrap("attributes", resolvedAttributes, "logger", LOGGER);
            script.setBinding(args);
            val result = script.execute(args.values().toArray(), Object.class);
            if (result != null) {
                LOGGER.debug("Mapped attribute [{}] to [{}] from script", attributeName, result);
                attributesToRelease.put(attributeName, CollectionUtils.wrapList(result));
            } else {
                LOGGER.warn("Groovy-scripted attribute returned no value for [{}]", attributeName);
            }
        });
    }

    @Override
    public List<String> determineRequestedAttributeDefinitions(final RegisteredServiceAttributeReleasePolicyContext context) {
        return new ArrayList<>(getAllowedAttributes().keySet());
    }
}
