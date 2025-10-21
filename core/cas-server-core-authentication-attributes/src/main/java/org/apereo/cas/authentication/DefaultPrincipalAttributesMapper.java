package org.apereo.cas.authentication;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link DefaultPrincipalAttributesMapper}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
public class DefaultPrincipalAttributesMapper implements PrincipalAttributesMapper {

    private static Map<String, List<Object>> fetchAttributeValueFromExternalGroovyScript(final String attributeName,
                                                                                         final Map<String, List<Object>> resolvedAttributes,
                                                                                         final String file) {
        return ApplicationContextProvider.getScriptResourceCacheManager()
            .map(cacheMgr -> {
                val script = cacheMgr.resolveScriptableResource(file, attributeName, file);
                return FunctionUtils.doIf(script != null,
                    Unchecked.supplier(() -> fetchAttributeValueFromScript(script, attributeName, resolvedAttributes)),
                    TreeMap<String, List<Object>>::new).get();
            })
            .orElseThrow(() -> new RuntimeException("No groovy script cache manager is available to execute attribute mappings"));
    }

    private static Map<String, List<Object>> fetchAttributeValueAsInlineGroovyScript(final String attributeName,
                                                                                     final Map<String, List<Object>> resolvedAttributes,
                                                                                     final String inlineGroovy) {
        return ApplicationContextProvider.getScriptResourceCacheManager()
            .map(cacheMgr -> FunctionUtils.doUnchecked(() -> {
                val script = cacheMgr.resolveScriptableResource(inlineGroovy, attributeName, inlineGroovy);
                return fetchAttributeValueFromScript(script, attributeName, resolvedAttributes);
            }))
            .orElseThrow(() -> new RuntimeException("No groovy script cache manager is available to execute attribute mappings"));
    }

    private static Map<String, List<Object>> mapSimpleSingleAttributeDefinition(final AttributeMappingRequest request) {
        val attributesToRelease = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        if (request.getAttributeValue() != null && !request.getAttributeValue().isEmpty()) {
            LOGGER.debug("Found attribute [{}] in the list of allowed attributes, mapped to the name [{}]",
                request.getAttributeName(), request.getMappedAttributeName());
            val values = CollectionUtils.toCollection(request.getAttributeValue(), ArrayList.class);
            attributesToRelease.put(request.getMappedAttributeName(), values);
        } else if (request.getResolvedAttributes().containsKey(request.getMappedAttributeName())) {
            val mappedValue = request.getResolvedAttributes().get(request.getMappedAttributeName());
            LOGGER.debug("Reusing existing already-remapped attribute [{}] with value [{}]", request.getMappedAttributeName(), mappedValue);
            attributesToRelease.put(request.getMappedAttributeName(), mappedValue);
        } else {
            LOGGER.warn("Could not find value for mapped attribute [{}] that is based off of [{}] in the allowed attributes list. "
                        + "Ensure the original attribute [{}] is retrieved and contains at least a single value. Attribute [{}] "
                        + "will and can not be released without the presence of a value.",
                request.getMappedAttributeName(), request.getAttributeName(),
                request.getAttributeName(), request.getMappedAttributeName());
        }
        return attributesToRelease;
    }

    private static Map<String, List<Object>> fetchAttributeValueFromScript(
        @NotNull final ExecutableCompiledScript script,
        final String attributeName,
        final Map<String, List<Object>> resolvedAttributes) {
        val attributesToRelease = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        val args = CollectionUtils.wrap("attributes", resolvedAttributes, "logger", LOGGER);
        script.setBinding(args);
        val result = script.execute(args.values().toArray(), Object.class, false);
        if (result != null) {
            LOGGER.debug("Mapped attribute [{}] to [{}] from script", attributeName, result);
            attributesToRelease.put(attributeName, CollectionUtils.wrapList(result));
        } else {
            LOGGER.warn("Groovy-scripted attribute returned no value for [{}]", attributeName);
        }
        return attributesToRelease;
    }

    @Override
    public Map<String, List<Object>> map(final AttributeMappingRequest request) {
        val scriptFactoryInstance = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();
        if (scriptFactoryInstance.isPresent()) {
            val scriptFactory = scriptFactoryInstance.get();
            if (scriptFactory.isInlineScript(request.getMappedAttributeName())) {
                val inlineGroovy = scriptFactory.getInlineScript(request.getMappedAttributeName()).orElseThrow();
                return fetchAttributeValueAsInlineGroovyScript(request.getAttributeName(), request.getResolvedAttributes(), inlineGroovy);
            }
            if (scriptFactory.isExternalScript(request.getMappedAttributeName())) {
                val file = scriptFactory.getExternalScript(request.getMappedAttributeName()).orElseThrow();
                return fetchAttributeValueFromExternalGroovyScript(request.getAttributeName(), request.getResolvedAttributes(), file);
            }
        }
        return mapSimpleSingleAttributeDefinition(request);
    }
}
