package org.apereo.cas.services;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apereo.cas.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link GroovyScriptAttributeReleasePolicy} that attempts to release attributes
 * based on the execution result of an external groovy script.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GroovyScriptAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyScriptAttributeReleasePolicy.class);
    private static final long serialVersionUID = 1703080077563402223L;

    private String groovyScript;

    public GroovyScriptAttributeReleasePolicy() {
    }

    public GroovyScriptAttributeReleasePolicy(final String groovyScript) {
        this.groovyScript = groovyScript;
    }

    public String getGroovyScript() {
        return groovyScript;
    }

    public void setGroovyScript(final String groovyScript) {
        this.groovyScript = groovyScript;
    }

    @Override
    protected Map<String, Object> getAttributesInternal(final Map<String, Object> attributes,
                                                        final RegisteredService service) {
        final ClassLoader parent = getClass().getClassLoader();
        try (GroovyClassLoader loader = new GroovyClassLoader(parent)) {
            final File groovyFile = ResourceUtils.getResourceFrom(this.groovyScript).getFile();
            if (groovyFile.exists()) {
                final Class<?> groovyClass = loader.parseClass(groovyFile);
                final GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
                LOGGER.debug("Created groovy object instance from class [{}]", groovyFile.getCanonicalPath());
                final Object[] args = {attributes, LOGGER};
                LOGGER.debug("Executing groovy script's run method, with parameters [{}]", args);
                final Map<String, Object> personAttributesMap = (Map<String, Object>) groovyObject.invokeMethod("run", args);
                LOGGER.debug("Final set of attributes determined by the script are [{}]", personAttributesMap);
                return personAttributesMap;
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.warn("Groovy script [{}] does not exist, or cannot be loaded", groovyScript);
        return new HashMap<>();
    }
}
