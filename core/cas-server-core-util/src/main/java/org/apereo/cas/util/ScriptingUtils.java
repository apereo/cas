package org.apereo.cas.util;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.File;

/**
 * This is {@link ScriptingUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public final class ScriptingUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptingUtils.class);

    private ScriptingUtils() {
    }

    /**
     * Execute groovy script via run object.
     *
     * @param <T>          the type parameter
     * @param groovyScript the groovy script
     * @param args         the args
     * @param clazz        the clazz
     * @return the object
     */
    public static <T> T executeGroovyScript(final Resource groovyScript,
                                            final Object[] args, final Class<T> clazz) {
        return executeGroovyScript(groovyScript, "run", args, clazz);
    }

    /**
     * Execute groovy script t.
     *
     * @param <T>          the type parameter
     * @param groovyScript the groovy script
     * @param methodName   the method name
     * @param args         the args
     * @param clazz        the clazz
     * @return the t
     */
    public static <T> T executeGroovyScript(final Resource groovyScript,
                                            final String methodName,
                                            final Object[] args,
                                            final Class<T> clazz) {

        if (groovyScript == null || StringUtils.isBlank(methodName)) {
            return null;
        }

        final ClassLoader parent = ScriptingUtils.class.getClassLoader();
        try (GroovyClassLoader loader = new GroovyClassLoader(parent)) {
            final File groovyFile = groovyScript.getFile();
            if (groovyFile.exists()) {                  
                final Class<?> groovyClass = loader.parseClass(groovyFile);
                LOGGER.trace("Creating groovy object instance from class [{}]", groovyFile.getCanonicalPath());

                final GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();

                LOGGER.trace("Executing groovy script's [{}] method, with parameters [{}]", methodName, args);
                final T result = (T) groovyObject.invokeMethod(methodName, args);
                LOGGER.trace("Results returned by the groovy script are [{}]", result);

                if (!clazz.isAssignableFrom(result.getClass())) {
                    throw new ClassCastException("Result [" + result
                            + " is of type " + result.getClass()
                            + " when we were expecting " + clazz);
                }
                return result;
            } else {
                LOGGER.trace("Groovy script at [{}] does not exist", groovyScript);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
