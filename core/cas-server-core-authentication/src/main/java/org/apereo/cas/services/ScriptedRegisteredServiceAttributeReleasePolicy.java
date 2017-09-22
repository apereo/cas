package org.apereo.cas.services;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is {@link ScriptedRegisteredServiceAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ScriptedRegisteredServiceAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
    private static final long serialVersionUID = -979532578142774128L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptedRegisteredServiceAttributeReleasePolicy.class);
    private static final Pattern INLINE_GROOVY_PATTERN = RegexUtils.createPattern("groovy\\s*\\{(.+)\\}");

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
                return new HashMap<>();
            }
            final Matcher matcherInline = INLINE_GROOVY_PATTERN.matcher(this.scriptFile);
            if (matcherInline.find()) {
                return getAttributesFromInlineGroovyScript(attributes, matcherInline);
            }
            return getScriptedAttributesFromFile(attributes);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashMap<>();
    }

    private static Map<String, Object> getAttributesFromInlineGroovyScript(final Map<String, Object> attributes,
                                                                           final Matcher matcherInline) throws ScriptException {
        final String script = matcherInline.group(1).trim();
        final ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");
        if (engine == null) {
            LOGGER.warn("Script engine is not available for Groovy");
            return new HashMap<>();
        }
        final Object[] args = {attributes, LOGGER};
        LOGGER.debug("Executing script, with parameters [{}]", args);

        final Bindings binding = new SimpleBindings();
        binding.put("attributes", attributes);
        binding.put("logger", LOGGER);
        return (Map<String, Object>) engine.eval(script, binding);
    }

    private Map<String, Object> getScriptedAttributesFromFile(final Map<String, Object> attributes) throws Exception {
        final String engineName = getScriptEngineName();
        final ScriptEngine engine = new ScriptEngineManager().getEngineByName(engineName);
        if (engine == null || StringUtils.isBlank(engineName)) {
            LOGGER.warn("Script engine is not available for [{}]", engineName);
            return new HashMap<>();
        }

        final File theScriptFile = ResourceUtils.getResourceFrom(this.scriptFile).getFile();
        if (theScriptFile.exists()) {
            LOGGER.debug("Created object instance from class [{}]", theScriptFile.getCanonicalPath());
            final Object[] args = {attributes, LOGGER};

            LOGGER.debug("Executing  script's run method, with parameters [{}]", args);
            engine.eval(new FileReader(theScriptFile));
            final Invocable invocable = (Invocable) engine;
            final Map<String, Object> personAttributesMap = (Map<String, Object>) invocable.invokeFunction("run", args);

            LOGGER.debug("Final set of attributes determined by the script are [{}]", personAttributesMap);
            return personAttributesMap;
        }
        LOGGER.warn("[{}] script [{}] does not exist, or cannot be loaded", StringUtils.capitalize(engineName), scriptFile);
        return new HashMap<>();
    }

    private String getScriptEngineName() {
        String engineName = null;
        if (this.scriptFile.endsWith(".py")) {
            engineName = "python";
        } else if (this.scriptFile.endsWith(".js")) {
            engineName = "js";
        } else if (this.scriptFile.endsWith(".groovy")) {
            engineName = "groovy";
        }
        return engineName;
    }
}
