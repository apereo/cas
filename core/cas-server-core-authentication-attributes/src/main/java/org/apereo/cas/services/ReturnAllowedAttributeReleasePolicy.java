package org.apereo.cas.services;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.GroovyShellScript;
import org.apereo.cas.util.scripting.ScriptingUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    private List<String> allowedAttributes = new ArrayList<>(0);

    @Override
    public Map<String, List<Object>> getAttributesInternal(
        final RegisteredServiceAttributeReleasePolicyContext context,
        final Map<String, List<Object>> attributes) {
        return authorizeReleaseOfAllowedAttributes(context, attributes);
    }

    @Override
    protected List<String> determineRequestedAttributeDefinitions(final RegisteredServiceAttributeReleasePolicyContext context) {
        return getAllowedAttributes().stream().filter(key -> !ScriptingUtils.isInlineGroovyScript(key)).collect(Collectors.toList());
    }

    protected Map<String, List<Object>> authorizeReleaseOfAllowedAttributes(
        final RegisteredServiceAttributeReleasePolicyContext context,
        final Map<String, List<Object>> attributes) {
        val resolvedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attributes);
        val attributesToRelease = new HashMap<String, List<Object>>();
        getAllowedAttributes().forEach(attr -> {
            if (resolvedAttributes.containsKey(attr)) {
                LOGGER.debug("Found attribute [{}] in the list of allowed attributes", attr);
                attributesToRelease.put(attr, resolvedAttributes.get(attr));
            } else {
                val matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(attr);
                if (matcherInline.find() && CasRuntimeHintsRegistrar.notInNativeImage()) {
                    val inlineGroovy = matcherInline.group(1);
                    try (val executableScript = new GroovyShellScript(inlineGroovy)) {
                        val args = CollectionUtils.<String, Object>wrap(
                            "context", context,
                            "attributes", attributes,
                            "logger", LOGGER);
                        executableScript.setBinding(args);
                        val scriptedAttributes = executableScript.execute(args.values().toArray(), Map.class);
                        attributesToRelease.putAll(scriptedAttributes);
                    }
                }
            }
        });
        return attributesToRelease;
    }
}
