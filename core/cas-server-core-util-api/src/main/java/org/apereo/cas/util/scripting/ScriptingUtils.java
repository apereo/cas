package org.apereo.cas.util.scripting;

import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.ResourceUtils;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.springframework.core.io.Resource;

import javax.script.Invocable;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is {@link ScriptingUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */

@Slf4j
@UtilityClass
public class ScriptingUtils {
    /**
     * Pattern indicating groovy script is inlined.
     */
    private static final Pattern INLINE_GROOVY_PATTERN = RegexUtils.createPattern("groovy\\s*\\{\\s*(.+)\\s*\\}",
        Pattern.DOTALL | Pattern.MULTILINE);

    /**
     * Pattern indicating groovy script is a file/resource.
     */
    private static final Pattern FILE_GROOVY_PATTERN = RegexUtils.createPattern("(file|classpath):(.+\\.groovy)");

    /**
     * Is inline groovy script ?.
     *
     * @param script the script
     * @return true/false
     */
    public static boolean isInlineGroovyScript(final String script) {
        return getMatcherForInlineGroovyScript(script).find();
    }

    /**
     * Is external groovy script ?.
     *
     * @param script the script
     * @return true/false
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
     * @param <T>    the type parameter
     * @param script the script
     * @param clazz  the clazz
     * @return the t
     */
    public static <T> T executeGroovyShellScript(final Script script,
                                                 final Class<T> clazz) {
        return executeGroovyShellScript(script, new HashMap<>(0), clazz);
    }

    /**
     * Execute groovy shell script t.
     *
     * @param <T>       the type parameter
     * @param script    the script
     * @param variables the variables
     * @param clazz     the clazz
     * @return the t
     */
    public static <T> T executeGroovyShellScript(final Script script,
                                                 final Map<String, Object> variables,
                                                 final Class<T> clazz) {
        try {
            val binding = script.getBinding();
            if (!binding.hasVariable("logger")) {
                binding.setVariable("logger", LOGGER);
            }
            if (variables != null && !variables.isEmpty()) {
                variables.forEach(binding::setVariable);
            }
            script.setBinding(binding);
            LOGGER.debug("Executing groovy script [{}] with variables [{}]", script, binding.getVariables());

            val result = script.run();
            return getGroovyScriptExecutionResultOrThrow(clazz, result);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
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
     * @param failOnError  the fail on error
     * @return the object
     */
    public static <T> T executeGroovyScript(final Resource groovyScript,
                                            final Object[] args, final Class<T> clazz,
                                            final boolean failOnError) {
        return executeGroovyScript(groovyScript, "run", args, clazz, failOnError);
    }

    /**
     * Execute groovy script.
     *
     * @param <T>          the type parameter
     * @param groovyObject the groovy object
     * @param args         the args
     * @param clazz        the clazz
     * @param failOnError  the fail on error
     * @return the result
     */
    public static <T> T executeGroovyScript(final GroovyObject groovyObject,
                                            final Object[] args, final Class<T> clazz,
                                            final boolean failOnError) {
        return executeGroovyScript(groovyObject, "run", args, clazz, failOnError);
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
        return executeGroovyScript(groovyScript, methodName, args, clazz, false);
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
        return executeGroovyScript(groovyScript, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, clazz, false);
    }

    /**
     * Execute groovy script t.
     *
     * @param <T>          the type parameter
     * @param groovyScript the groovy script
     * @param methodName   the method name
     * @param args         the args
     * @param clazz        the clazz
     * @param failOnError  the fail on error
     * @return the t
     */
    @SneakyThrows
    public static <T> T executeGroovyScript(final Resource groovyScript,
                                            final String methodName,
                                            final Object[] args,
                                            final Class<T> clazz,
                                            final boolean failOnError) {

        if (groovyScript == null || StringUtils.isBlank(methodName)) {
            return null;
        }

        try {
            return AccessController.doPrivileged((PrivilegedAction<T>) () -> getGroovyResult(groovyScript, methodName, args, clazz, failOnError));
        } catch (final Exception e) {
            var cause = e instanceof PrivilegedActionException ? PrivilegedActionException.class.cast(e).getException() : e;
            if (failOnError) {
                throw cause;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(cause.getMessage(), cause);
            } else {
                LOGGER.error(cause.getMessage());
            }
        }
        return null;
    }

    /**
     * Execute groovy script t.
     *
     * @param <T>          the type parameter
     * @param groovyObject the groovy object
     * @param methodName   the method name
     * @param args         the args
     * @param clazz        the clazz
     * @param failOnError  the fail on error
     * @return the t
     */
    @SneakyThrows
    public static <T> T executeGroovyScript(final GroovyObject groovyObject,
                                            final String methodName,
                                            final Object[] args,
                                            final Class<T> clazz,
                                            final boolean failOnError) {
        try {
            LOGGER.trace("Executing groovy script's [{}] method, with parameters [{}]", methodName, args);
            val result = groovyObject.invokeMethod(methodName, args);
            LOGGER.trace("Results returned by the groovy script are [{}]", result);
            if (!clazz.equals(Void.class)) {
                return getGroovyScriptExecutionResultOrThrow(clazz, result);
            }
        } catch (final Exception e) {
            var cause = e instanceof InvokerInvocationException ? e.getCause() : e;
            if (failOnError) {
                throw cause;
            }
            LOGGER.error(cause.getMessage(), cause);
        }
        return null;
    }

    /**
     * Parse groovy shell script script.
     *
     * @param script the script
     * @return the script
     */
    public static Script parseGroovyShellScript(final String script) {
        try {
            val shell = new GroovyShell();
            LOGGER.debug("Parsing groovy script [{}]", script);
            return shell.parse(script);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Parse groovy script groovy object.
     *
     * @param groovyScript the groovy script
     * @param failOnError  the fail on error
     * @return the groovy object
     */
    public static GroovyObject parseGroovyScript(final Resource groovyScript,
                                                 final boolean failOnError) {
        return AccessController.doPrivileged((PrivilegedAction<GroovyObject>) () -> {
            val parent = ScriptingUtils.class.getClassLoader();
            try (val loader = new GroovyClassLoader(parent)) {
                val groovyClass = loadGroovyClass(groovyScript, loader);
                if (groovyClass != null) {
                    LOGGER.trace("Creating groovy object instance from class [{}]", groovyScript.getURI().getPath());
                    return (GroovyObject) groovyClass.getDeclaredConstructor().newInstance();
                }
                LOGGER.warn("Groovy script at [{}] does not exist", groovyScript.getURI().getPath());
            } catch (final Exception e) {
                if (failOnError) {
                    throw new RuntimeException(e);
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error(e.getMessage(), e);
                } else {
                    LOGGER.error(e.getMessage());
                }
            }
            return null;
        });
    }

    private Class loadGroovyClass(final Resource groovyScript,
                                  final GroovyClassLoader loader) throws IOException {
        if (ResourceUtils.isJarResource(groovyScript)) {
            try (val groovyReader = new BufferedReader(new InputStreamReader(groovyScript.getInputStream(), StandardCharsets.UTF_8))) {
                return loader.parseClass(groovyReader, groovyScript.getFilename());
            }
        }

        val groovyFile = groovyScript.getFile();
        if (groovyFile.exists()) {
            return loader.parseClass(groovyFile);
        }
        return null;
    }


    @SneakyThrows
    private static <T> T getGroovyResult(final Resource groovyScript,
                                         final String methodName,
                                         final Object[] args,
                                         final Class<T> clazz,
                                         final boolean failOnError) {
        try {
            val groovyObject = parseGroovyScript(groovyScript, failOnError);
            if (groovyObject == null) {
                LOGGER.error("Could not parse the Groovy script at [{}]", groovyScript);
                return null;
            }
            return executeGroovyScript(groovyObject, methodName, args, clazz, failOnError);
        } catch (final Exception e) {
            if (failOnError) {
                throw e;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    private static <T> T getGroovyScriptExecutionResultOrThrow(final Class<T> clazz, final Object result) {
        if (result != null && !clazz.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Result [" + result + " is of type " + result.getClass() + " when we were expecting " + clazz);
        }
        return (T) result;
    }

    /**
     * Execute groovy script engine t.
     *
     * @param <T>        the type parameter
     * @param scriptFile the script file
     * @param args       the args
     * @param clazz      the clazz
     * @return the t
     */
    public static <T> T executeScriptEngine(final String scriptFile, final Object[] args, final Class<T> clazz) {
        try {
            val engineName = getScriptEngineName(scriptFile);
            if (StringUtils.isBlank(engineName)) {
                LOGGER.warn("Script engine name can not be determined for [{}]", engineName);
                return null;
            }
            val engine = new ScriptEngineManager().getEngineByName(engineName);
            if (engine == null) {
                LOGGER.warn("Script engine is not available for [{}]", engineName);
                return null;
            }

            val resourceFrom = ResourceUtils.getResourceFrom(scriptFile);
            val theScriptFile = resourceFrom.getFile();
            if (theScriptFile.exists()) {
                LOGGER.debug("Created object instance from class [{}]", theScriptFile.getCanonicalPath());

                try (val reader = Files.newBufferedReader(theScriptFile.toPath(), StandardCharsets.UTF_8)) {
                    engine.eval(reader);
                }
                val invocable = (Invocable) engine;
                LOGGER.debug("Executing script's run method, with parameters [{}]", args);
                val result = invocable.invokeFunction("run", args);
                LOGGER.debug("Groovy script result is [{}]", result);
                return getGroovyScriptExecutionResultOrThrow(clazz, result);
            }
            LOGGER.warn("[{}] script [{}] does not exist, or cannot be loaded", StringUtils.capitalize(engineName), scriptFile);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Execute inline groovy script engine.
     *
     * @param <T>       the type parameter
     * @param script    the script
     * @param variables the variables
     * @param clazz     the clazz
     * @return the t
     */
    public static <T> T executeGroovyScriptEngine(final String script,
                                                  final Map<String, Object> variables,
                                                  final Class<T> clazz) {
        try {
            val engine = new ScriptEngineManager().getEngineByName("groovy");
            if (engine == null) {
                LOGGER.warn("Script engine is not available for Groovy");
                return null;
            }
            val binding = new SimpleBindings();
            if (variables != null && !variables.isEmpty()) {
                binding.putAll(variables);
            }
            if (!binding.containsKey("logger")) {
                binding.put("logger", LOGGER);
            }
            val result = engine.eval(script, binding);
            return getGroovyScriptExecutionResultOrThrow(clazz, result);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Gets object instance from groovy resource.
     *
     * @param <T>          the type parameter
     * @param resource     the resource
     * @param expectedType the expected type
     * @return the object instance from groovy resource
     */
    public static <T> T getObjectInstanceFromGroovyResource(final Resource resource,
                                                            final Class<T> expectedType) {
        return getObjectInstanceFromGroovyResource(resource, ArrayUtils.EMPTY_CLASS_ARRAY, ArrayUtils.EMPTY_OBJECT_ARRAY, expectedType);
    }

    /**
     * Gets object instance from groovy resource.
     *
     * @param <T>             the type parameter
     * @param resource        the resource
     * @param constructorArgs the constructor args
     * @param args            the args
     * @param expectedType    the expected type
     * @return the object instance from groovy resource
     */
    public static <T> T getObjectInstanceFromGroovyResource(final Resource resource,
                                                            final Class[] constructorArgs,
                                                            final Object[] args,
                                                            final Class<T> expectedType) {
        try {
            if (resource == null) {
                LOGGER.debug("No groovy script is defined");
                return null;
            }

            val script = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);

            val clazz = AccessController.doPrivileged((PrivilegedAction<Class<T>>) () -> {
                val classLoader = new GroovyClassLoader(ScriptingUtils.class.getClassLoader(),
                    new CompilerConfiguration(), true);
                return classLoader.parseClass(script);
            });

            LOGGER.trace("Preparing constructor arguments [{}] for resource [{}]", args, resource);
            val ctor = clazz.getDeclaredConstructor(constructorArgs);
            val result = ctor.newInstance(args);

            if (!expectedType.isAssignableFrom(result.getClass())) {
                throw new ClassCastException("Result [" + result
                    + " is of type " + result.getClass()
                    + " when we were expecting " + expectedType);
            }
            return result;
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Gets script engine name.
     *
     * @param scriptFile the script file
     * @return the script engine name
     */
    public static String getScriptEngineName(final String scriptFile) {
        if (scriptFile.endsWith(".py")) {
            return "python";
        }
        if (scriptFile.endsWith(".js")) {
            return "js";
        }
        if (scriptFile.endsWith(".groovy")) {
            return "groovy";
        }
        return null;
    }
}
