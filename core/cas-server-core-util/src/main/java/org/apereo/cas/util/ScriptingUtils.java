package org.apereo.cas.util;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.io.File;
import java.io.FileReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is {@link ScriptingUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public final class ScriptingUtils {
    /**
     * Pattern indicating groovy script is inlined.
     */
    private static final Pattern INLINE_GROOVY_PATTERN = RegexUtils.createPattern("groovy\\s*\\{(.+)\\}");

    /**
     * Pattern indicating groovy script is a file/resource.
     */
    private static final Pattern FILE_GROOVY_PATTERN = RegexUtils.createPattern("file:(.+\\.groovy)");

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptingUtils.class);

    private ScriptingUtils() {
    }

    /**
     * Is inline groovy script ?.
     *
     * @param script the script
     * @return the boolean
     */
    public static boolean isInlineGroovyScript(final String script) {
        return getMatcherForInlineGroovyScript(script).find();
    }

    /**
     * Is external groovy script ?.
     *
     * @param script the script
     * @return the boolean
     */
    public static boolean isExternalGroovyScript(final String script) {
        return getMatcherForExternalGroovyScript(script).find();
    }

    /**
     * Gets inline groovy script matcher.
     *
     * @param script the script
     * @return the inline groovy script matcher
     */
    public static Matcher getMatcherForInlineGroovyScript(final String script) {
        return INLINE_GROOVY_PATTERN.matcher(script);
    }

    /**
     * Gets groovy file script matcher.
     *
     * @param script the script
     * @return the groovy file script matcher
     */
    public static Matcher getMatcherForExternalGroovyScript(final String script) {
        return FILE_GROOVY_PATTERN.matcher(script);
    }

    /**
     * Execute groovy shell script t.
     *
     * @param <T>       the type parameter
     * @param script    the script
     * @param variables the variables
     * @return the t
     */
    public static <T> T executeGroovyShellScript(final String script,
                                                 final Map<String, Object> variables) {
        try {
            final Binding binding = new Binding();
            final GroovyShell shell = new GroovyShell(binding);
            if (variables != null && !variables.isEmpty()) {
                variables.forEach(binding::setVariable);
            }
            if (!binding.hasVariable("logger")) {
                binding.setVariable("logger", LOGGER);
            }
            LOGGER.debug("Executing groovy script [{}] with variables [{}]", script, binding.getVariables());
            return (T) shell.evaluate(script);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
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
     * @param clazz        the clazz
     * @param args         the args
     * @return the type to return
     */
    public static <T> T executeGroovyScript(final Resource groovyScript,
                                            final String methodName,
                                            final Class<T> clazz,
                                            final Object... args) {
        return executeGroovyScript(groovyScript, methodName, args, clazz);
    }

    /**
     * Execute groovy script.
     *
     * @param <T>          the type parameter
     * @param groovyScript the groovy script
     * @param methodName   the method name
     * @param clazz        the clazz
     * @return the t
     */
    public static <T> T executeGroovyScript(final Resource groovyScript,
                                            final String methodName,
                                            final Class<T> clazz) {
        return executeGroovyScript(groovyScript, methodName, new Object[] {}, clazz);
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
        return AccessController.doPrivileged((PrivilegedAction<T>) () -> getGroovyResult(groovyScript, methodName, args, clazz, parent));
    }

    private static <T> T getGroovyResult(final Resource groovyScript, final String methodName,
                                         final Object[] args, final Class<T> clazz, final ClassLoader parent) {
        try (GroovyClassLoader loader = new GroovyClassLoader(parent)) {
            final File groovyFile = groovyScript.getFile();
            if (groovyFile.exists()) {
                final Class<?> groovyClass = loader.parseClass(groovyFile);
                LOGGER.trace("Creating groovy object instance from class [{}]", groovyFile.getCanonicalPath());

                final GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();

                LOGGER.trace("Executing groovy script's [{}] method, with parameters [{}]", methodName, args);
                final T result = (T) groovyObject.invokeMethod(methodName, args);
                LOGGER.trace("Results returned by the groovy script are [{}]", result);

                if (result != null && !clazz.isAssignableFrom(result.getClass())) {
                    throw new ClassCastException("Result [" + result + " is of type " + result.getClass() + " when we were expecting " + clazz);
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

    /**
     * Execute groovy script engine t.
     *
     * @param <T>        the type parameter
     * @param scriptFile the script file
     * @param args       the args
     * @return the t
     */
    public static <T> T executeGroovyScriptEngine(final String scriptFile, final Object[] args) {
        try {
            final String engineName = getScriptEngineName(scriptFile);
            final ScriptEngine engine = new ScriptEngineManager().getEngineByName(engineName);
            if (engine == null || StringUtils.isBlank(engineName)) {
                LOGGER.warn("Script engine is not available for [{}]", engineName);
                return null;
            }

            final AbstractResource resourceFrom = ResourceUtils.getResourceFrom(scriptFile);
            final File theScriptFile = resourceFrom.getFile();
            if (theScriptFile.exists()) {
                LOGGER.debug("Created object instance from class [{}]", theScriptFile.getCanonicalPath());

                engine.eval(new FileReader(theScriptFile));
                final Invocable invocable = (Invocable) engine;

                LOGGER.debug("Executing script's run method, with parameters [{}]", args);
                final T result = (T) invocable.invokeFunction("run", args);
                LOGGER.debug("Groovy script result is [{}]", result);
                return result;
            }
            LOGGER.warn("[{}] script [{}] does not exist, or cannot be loaded", StringUtils.capitalize(engineName), scriptFile);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Execute inline groovy script engine.
     *
     * @param <T>       the type parameter
     * @param script    the script
     * @param variables the variables
     * @return the t
     */
    public static <T> T executeGroovyScriptEngine(final String script,
                                                  final Map<String, Object> variables) {
        try {
            final ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");
            if (engine == null) {
                LOGGER.warn("Script engine is not available for Groovy");
                return null;
            }
            final Bindings binding = new SimpleBindings();
            if (variables != null && !variables.isEmpty()) {
                binding.putAll(variables);
            }
            if (!binding.containsKey("logger")) {
                binding.put("logger", LOGGER);
            }
            return (T) engine.eval(script, binding);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private static String getScriptEngineName(final String scriptFile) {
        String engineName = null;
        if (scriptFile.endsWith(".py")) {
            engineName = "python";
        } else if (scriptFile.endsWith(".js")) {
            engineName = "js";
        } else if (scriptFile.endsWith(".groovy")) {
            engineName = "groovy";
        }
        return engineName;
    }
}
