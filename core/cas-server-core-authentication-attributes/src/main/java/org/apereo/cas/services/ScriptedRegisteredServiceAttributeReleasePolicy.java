package org.apereo.cas.services;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * This is {@link ScriptedRegisteredServiceAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ScriptedRegisteredServiceAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
    private static final long serialVersionUID = -979532578142774128L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptedRegisteredServiceAttributeReleasePolicy.class);

    private String scriptFile;

    public ScriptedRegisteredServiceAttributeReleasePolicy() {
    }

    public ScriptedRegisteredServiceAttributeReleasePolicy(final String scriptFile) {
        this.scriptFile = scriptFile;
    }

    public String getScriptFile() {
        return scriptFile;
    }

    public void setScriptFile(final String scriptFile) {
        this.scriptFile = scriptFile;
    }

    @Override
    protected Map<String, Object> getAttributesInternal(final Principal principal,
                                                        final Map<String, Object> attributes,
                                                        final RegisteredService service) {
        try {
            if (StringUtils.isBlank(this.scriptFile)) {
                return new HashMap<>(0);
            }
            final Matcher matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(this.scriptFile);
            if (matcherInline.find()) {
                return getAttributesFromInlineGroovyScript(attributes, matcherInline);
            }
            return getScriptedAttributesFromFile(attributes);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashMap<>(0);
    }

    private static Map<String, Object> getAttributesFromInlineGroovyScript(final Map<String, Object> attributes,
                                                                           final Matcher matcherInline) {
        final String script = matcherInline.group(1).trim();
        final Map<String, Object> map = ScriptingUtils.executeGroovyScriptEngine(script,
                CollectionUtils.wrap("attributes", attributes, "logger", LOGGER));
        return ObjectUtils.defaultIfNull(map, new HashMap<>());
    }

    private Map<String, Object> getScriptedAttributesFromFile(final Map<String, Object> attributes) {
        final Object[] args = {attributes, LOGGER};
        final Map<String, Object> map = ScriptingUtils.executeGroovyScriptEngine(this.scriptFile, args);
        return ObjectUtils.defaultIfNull(map, new HashMap<>());
    }
}
