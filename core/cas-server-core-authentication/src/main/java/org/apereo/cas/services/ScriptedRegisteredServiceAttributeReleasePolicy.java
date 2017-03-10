package org.apereo.cas.services;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

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
    protected Map<String, Object> getAttributesInternal(final Map<String, Object> attributes,
                                                        final RegisteredService service) {
        try {
            String engineName = null;
            if (this.scriptFile.endsWith(".py")) {
                engineName = "python";
            } else if (this.scriptFile.endsWith(".js")) {
                engineName = "js";
            } else if (this.scriptFile.endsWith(".groovy")) {
                engineName = "groovy";
            }

            final ScriptEngine engine = new ScriptEngineManager().getEngineByName("python");
            if (engine == null || StringUtils.isBlank(engineName)) {
                LOGGER.warn("Script engine is not available for [{}]", engineName);
            } else {
                final File theScriptFile = ResourceUtils.getResourceFrom(this.scriptFile).getFile();
                if (theScriptFile.exists()) {

                    LOGGER.debug("Created python object instance from class [{}]", theScriptFile.getCanonicalPath());
                    final Object[] args = {attributes, LOGGER};

                    LOGGER.debug("Executing python script's run method, with parameters [{}]", args);

                    engine.eval(new FileReader(theScriptFile));
                    final Invocable invocable = (Invocable) engine;
                    final Map<String, Object> personAttributesMap = (Map<String, Object>) invocable.invokeFunction("run", args);

                    LOGGER.debug("Final set of attributes determined by the script are [{}]", personAttributesMap);
                    return personAttributesMap;
                }
                LOGGER.warn("Python script [{}] does not exist, or cannot be loaded", scriptFile);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashMap<>();
    }
}
