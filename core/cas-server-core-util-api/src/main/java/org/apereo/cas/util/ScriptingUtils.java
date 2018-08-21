package org.apereo.cas.util;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.springframework.core.io.Resource;

import javax.script.Invocable;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
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
     * @param clazz     the clazz
     * @return the t
     */
    public static <T> T executeGroovyShellScript(final String script,
                                                 final Map<String, Object> variables,
                                                 final Class<T> clazz) {
        try {
            val binding = new Binding();
            val shell = new GroovyShell(binding);
            if (variables != null && !variables.isEmpty()) {
                variables.forEach(binding::setVariable);
            }
            if (!binding.hasVariable("logger")) {
                binding.setVariable("logger", LOGGER);
            }
            LOGGER.debug("Executing groovy script [{}] with variables [{}]", script, binding.getVariables());

            val result = shell.evaluate(script);
            if (result != null && !clazz.isAssignableFrom(result.getClass())) {
                throw new ClassCastException("Result [" + result
                    + " is of type " + result.getClass()
                    + " when we were expecting " + clazz);
            }
            return (T) result;

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
     * @param failOnError  the fail on error
     * @return the object
     */
    public static <T> T executeGroovyScript(final Resource groovyScript,
                                            final Object[] args, final Class<T> clazz,
                                            final boolean failOnError) {
        return executeGroovyScript(groovyScript, "run", args, clazz, failOnError);
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
        return executeGroovyScript(groovyScript, methodName, new Object[]{}, clazz, false);
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

        val parent = ScriptingUtils.class.getClassLoader();
        try {
            return AccessController.doPrivileged((PrivilegedAction<T>) () -> getGroovyResult(groovyScript, methodName, args, clazz, parent, failOnError));
        } catch (final Exception e) {
            var cause = (Throwable) null;
            if (e instanceof PrivilegedActionException) {
                cause = PrivilegedActionException.class.cast(e).getException();
            } else {
                cause = e;
            }

            if (failOnError) {
                throw cause;
            }
            LOGGER.error(cause.getMessage(), cause);
        }
        return null;
    }

    @SneakyThrows
    private static <T> T getGroovyResult(final Resource groovyScript, final String methodName,
                                         final Object[] args, final Class<T> clazz, final ClassLoader parent,
                                         final boolean failOnError) {
        try (val loader = new GroovyClassLoader(parent)) {
            val groovyFile = groovyScript.getFile();
            if (groovyFile.exists()) {
                val groovyClass = loader.parseClass(groovyFile);
                LOGGER.trace("Creating groovy object instance from class [{}]", groovyFile.getCanonicalPath());

                val groovyObject = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance();
                LOGGER.trace("Executing groovy script's [{}] method, with parameters [{}]", methodName, args);
                val result = groovyObject.invokeMethod(methodName, args);
                LOGGER.trace("Results returned by the groovy script are [{}]", result);

                if (!clazz.equals(Void.class)) {
                    if (result != null && !clazz.isAssignableFrom(result.getClass())) {
                        throw new ClassCastException("Result [" + result + " is of type " + result.getClass() + " when we were expecting " + clazz);
                    }
                    return (T) result;
                }
                return null;
            }
            LOGGER.trace("Groovy script at [{}] does not exist", groovyScript);
        } catch (final Exception e) {
            var cause = (Throwable) null;
            if (e instanceof InvokerInvocationException) {
                cause = e.getCause();
            } else {
                cause = e;
            }
            if (failOnError) {
                throw cause;
            }
            LOGGER.error(cause.getMessage(), cause);
        }
        return null;
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
            val engine = new ScriptEngineManager().getEngineByName(engineName);
            if (engine == null || StringUtils.isBlank(engineName)) {
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
                if (result != null && !clazz.isAssignableFrom(result.getClass())) {
                    throw new ClassCastException("Result [" + result + " is of type " + result.getClass() + " when we were expecting " + clazz);
                }
                return (T) result;
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
            if (result != null && !clazz.isAssignableFrom(result.getClass())) {
                throw new ClassCastException("Result [" + result + " is of type " + result.getClass() + " when we were expecting " + clazz);
            }
            return (T) result;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
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
        return getObjectInstanceFromGroovyResource(resource, new Class[]{}, new Object[]{}, expectedType);
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

            LOGGER.debug("Preparing constructor arguments [{}] for resource [{}]", args, resource);
            val ctor = clazz.getDeclaredConstructor(constructorArgs);
            val result = ctor.newInstance(args);

            if (result != null && !expectedType.isAssignableFrom(result.getClass())) {
                throw new ClassCastException("Result [" + result
                    + " is of type " + result.getClass()
                    + " when we were expecting " + expectedType);
            }
            return result;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private static String getScriptEngineName(final String scriptFile) {
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
