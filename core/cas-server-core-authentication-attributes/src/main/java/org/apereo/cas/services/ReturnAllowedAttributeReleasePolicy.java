package org.apereo.cas.services;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Return only the collection of allowed attributes out of what's resolved
 * for the principal.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ReturnAllowedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = -5771481877391140569L;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> allowedAttributes = new ArrayList<>();

    @Override
    public Map<String, List<Object>> getAttributesInternal(
        final RegisteredServiceAttributeReleasePolicyContext context,
        final Map<String, List<Object>> attributes) throws Throwable {
        return authorizeReleaseOfAllowedAttributes(context, attributes);
    }

    @Override
    protected List<String> determineRequestedAttributeDefinitions(final RegisteredServiceAttributeReleasePolicyContext context) {
        val scriptFactory = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();
        return getAllowedAttributes()
            .stream()
            .filter(key -> scriptFactory.isEmpty() || !scriptFactory.get().isInlineScript(key))
            .collect(Collectors.toList());
    }

    protected Map<String, List<Object>> authorizeReleaseOfAllowedAttributes(
        final RegisteredServiceAttributeReleasePolicyContext context,
        final Map<String, List<Object>> attributes) {
        val resolvedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attributes);
        val attributesToRelease = new HashMap<String, List<Object>>();

        val scriptFactory = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();
        getAllowedAttributes().forEach(attr -> {
            if (resolvedAttributes.containsKey(attr)) {
                LOGGER.debug("Found attribute [{}] in the list of allowed attributes", attr);
                attributesToRelease.put(attr, resolvedAttributes.get(attr));
            } else if (CasRuntimeHintsRegistrar.notInNativeImage() && scriptFactory.isPresent()) {
                attributesToRelease.putAll(executeInlineGroovyScript(context, attributes, attr));
            }
        });
        return attributesToRelease;
    }

    private static Map<String, List<Object>> executeInlineGroovyScript(final RegisteredServiceAttributeReleasePolicyContext context,
                                                                       final Map<String, List<Object>> resolvedAttributes,
                                                                       final String attribute) {
        val scriptFactory = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();
        if (scriptFactory.isPresent() && scriptFactory.get().isInlineScript(attribute) && CasRuntimeHintsRegistrar.notInNativeImage()) {
            val inlineGroovy = scriptFactory.get().getInlineScript(attribute).orElseThrow();
            try (val executableScript = scriptFactory.get().fromScript(inlineGroovy)) {
                val args = CollectionUtils.<String, Object>wrap(
                    "context", context,
                    "attributes", resolvedAttributes,
                    "logger", LOGGER);
                executableScript.setBinding(args);
                return FunctionUtils.doUnchecked(() -> executableScript.execute(args.values().toArray(), Map.class));
            }
        }
        return new HashMap<>();
    }
}
