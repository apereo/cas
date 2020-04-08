package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * This is {@link ScriptedRegisteredServiceAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 * @deprecated Since 6.2
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Deprecated(since = "6.2.0")
public class ScriptedRegisteredServiceAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -979532578142774128L;

    private String scriptFile;

    private static Map<String, List<Object>> getAttributesFromInlineGroovyScript(final Map<String, List<Object>> attributes, final Matcher matcherInline) {
        val script = matcherInline.group(1).trim();
        val args = CollectionUtils.wrap("attributes", attributes, "logger", LOGGER);
        val map = ScriptingUtils.executeGroovyScriptEngine(script, args, Map.class);
        return ObjectUtils.defaultIfNull(map, new HashMap<>(0));
    }

    @Override
    public Map<String, List<Object>> getAttributesInternal(final Principal principal, final Map<String, List<Object>> attributes,
                                                     final RegisteredService service, final Service selectedService) {
        try {
            if (StringUtils.isBlank(this.scriptFile)) {
                return new HashMap<>(0);
            }
            val matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(this.scriptFile);
            if (matcherInline.find()) {
                return getAttributesFromInlineGroovyScript(attributes, matcherInline);
            }
            return getScriptedAttributesFromFile(attributes);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashMap<>(0);
    }

    private Map<String, List<Object>> getScriptedAttributesFromFile(final Map<String, List<Object>> attributes) {
        val args = new Object[]{attributes, LOGGER};
        val map = ScriptingUtils.executeScriptEngine(this.scriptFile, args, Map.class);
        return ObjectUtils.defaultIfNull(map, new HashMap<>(0));
    }
}
