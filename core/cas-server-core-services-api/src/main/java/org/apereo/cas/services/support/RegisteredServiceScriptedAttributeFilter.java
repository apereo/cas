package org.apereo.cas.services.support;

import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ScriptingUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link RegisteredServiceScriptedAttributeFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class RegisteredServiceScriptedAttributeFilter implements RegisteredServiceAttributeFilter {

    private static final long serialVersionUID = 122972056984610198L;

    private int order;

    private String script;

    private static Map<String, Object> getGroovyAttributeValue(final String groovyScript, final Map<String, Object> resolvedAttributes) {
        val args = CollectionUtils.wrap("attributes", resolvedAttributes, "logger", LOGGER);
        return ScriptingUtils.executeGroovyShellScript(groovyScript, args, Map.class);
    }

    private static Map<String, Object> filterInlinedGroovyAttributeValues(final Map<String, Object> resolvedAttributes, final String script) {
        LOGGER.debug("Found inline groovy script to execute [{}]", script);
        val attributesToRelease = getGroovyAttributeValue(script, resolvedAttributes);
        return attributesToRelease;
    }

    private static Map<String, Object> filterFileBasedGroovyAttributeValues(final Map<String, Object> resolvedAttributes, final String scriptFile) {
        try {
            LOGGER.debug("Found groovy script file to execute [{}]", scriptFile);
            val script = FileUtils.readFileToString(new File(scriptFile), StandardCharsets.UTF_8);
            val attributesToRelease = getGroovyAttributeValue(script, resolvedAttributes);
            return attributesToRelease;
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashMap<>(0);
    }

    @Override
    public Map<String, Object> filter(final Map<String, Object> givenAttributes) {
        val matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(script);
        val matcherFile = ScriptingUtils.getMatcherForExternalGroovyScript(script);
        if (matcherInline.find()) {
            return filterInlinedGroovyAttributeValues(givenAttributes, matcherInline.group(1));
        }
        if (matcherFile.find()) {
            return filterFileBasedGroovyAttributeValues(givenAttributes, matcherFile.group(2));
        }
        return givenAttributes;
    }

}
