package org.apereo.cas.util.scripting;

import module java.base;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;
import groovy.transform.CompileStatic;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;

/**
 * This is {@link ScriptingUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */

@Slf4j
@UtilityClass
public class ScriptingUtils {

    @SuppressWarnings("InlineFormatString")
    private static final String INLINE_PATTERN = "%s\\s*\\{\\s*(.+)\\s*\\}";

    @SuppressWarnings("InlineFormatString")
    private static final String FILE_PATTERN = "(file|classpath):(.+\\.%s)";

    /**
     * Pattern indicating groovy script is inlined.
     */
    private static final Pattern INLINE_GROOVY_PATTERN = RegexUtils.createPattern(String.format(INLINE_PATTERN, "groovy"),
        Pattern.DOTALL | Pattern.MULTILINE);

    /**
     * Pattern indicating groovy script is a file/resource.
     */
    private static final Pattern FILE_GROOVY_PATTERN = RegexUtils.createPattern(String.format(FILE_PATTERN, "groovy"));

    private static final Map<String, CompiledGroovyClass> OBJECT_CLASSES = new ConcurrentHashMap<>();

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
    public static <T> @Nullable T executeGroovyShellScript(final Script script,
                                                           final Class<T> clazz) {
        return executeGroovyShellScript(script, new HashMap<>(), clazz);
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
    public static <T> @Nullable T executeGroovyShellScript(final Script script,
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
            LoggingUtils.error(LOGGER, e);
        }
        return null;
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
     * @throws Throwable the exception
     */
    public static <T> @Nullable T executeGroovyScript(final GroovyObject groovyObject,
                                                      final Object[] args, final Class<T> clazz,
                                                      final boolean failOnError) throws Throwable {
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
    public static <T> @Nullable T executeGroovyScript(final Resource groovyScript,
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
    public static <T> @Nullable T executeGroovyScript(final Resource groovyScript,
                                                      final String methodName,
                                                      final Class<T> clazz) {
        return executeGroovyScript(groovyScript, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, clazz, false);
    }

    /**
     * Execute groovy script.
     *
     * @param <T>          the type parameter
     * @param groovyScript the groovy script
     * @param methodName   the method name
     * @param args         the args
     * @param clazz        the clazz
     * @param failOnError  the fail on error
     * @return the t
     */
    public static <T> @Nullable T executeGroovyScript(final Resource groovyScript,
                                                      final String methodName,
                                                      final Object[] args,
                                                      final Class<T> clazz,
                                                      final boolean failOnError) {
        try {
            if (groovyScript == null || StringUtils.isBlank(methodName)) {
                return null;
            }
            return getGroovyResult(groovyScript, methodName, args, clazz, failOnError);
        } catch (final Throwable e) {
            if (failOnError) {
                throw new RuntimeException(e);
            }
            LoggingUtils.error(LOGGER, e);
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
     * @throws Throwable the throwable
     */
    public static <T> @Nullable T executeGroovyScript(final GroovyObject groovyObject,
                                                      final String methodName,
                                                      final Object[] args,
                                                      final Class<T> clazz,
                                                      final boolean failOnError) throws Throwable {
        try {
            LOGGER.trace("Executing groovy script's [{}] method, with parameters [{}]", methodName, args);
            val result = groovyObject.invokeMethod(methodName, args);
            LOGGER.trace("Results returned by the groovy script are [{}]", result);
            if (!clazz.equals(Void.class)) {
                return getGroovyScriptExecutionResultOrThrow(clazz, result);
            }
        } catch (final Throwable throwable) {
            val cause = throwable instanceof InvokerInvocationException ? throwable.getCause() : throwable;
            if (failOnError) {
                throw cause;
            }
            if (cause instanceof MissingMethodException) {
                LOGGER.debug(cause.getMessage(), cause);
            } else {
                LoggingUtils.error(LOGGER, cause);
            }
        }
        return null;
    }

    /**
     * Parse groovy shell script.
     *
     * @param script the script
     * @return the script
     */
    public static Script parseGroovyShellScript(@Nullable final Map inputVariables, final String script) {
        val variables = inputVariables != null ? new HashMap<>(inputVariables) : new HashMap<>();
        variables.putIfAbsent("logger", LOGGER);
        val binding = new Binding(variables);
        val shell = new GroovyShell(binding, createCompilerConfiguration());
        LOGGER.debug("Parsing groovy script [{}]", script);
        return shell.parse(script, binding);
    }

    /**
     * Parse groovy shell script.
     *
     * @param script the script
     * @return the script
     */
    public static @Nullable Script parseGroovyShellScript(final String script) {
        return StringUtils.isNotBlank(script) ? parseGroovyShellScript(Map.of(), script) : null;
    }

    /**
     * Parse groovy script groovy object.
     *
     * @param groovyScript the groovy script
     * @param failOnError  the fail on error
     * @return the groovy object
     */
    public static @Nullable GroovyObject parseGroovyScript(final Resource groovyScript,
                                                           final boolean failOnError) {
        try (val loader = newGroovyClassLoader()) {
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
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    /**
     * Validate the syntax of the given groovy script.
     * <p>
     * Compiling a script is not a safe way to validate it. Groovy applies AST transformations
     * inside the compiler, and a transformation that the script itself declares, such as
     * {@code ASTTest} or {@code Grab}, runs arbitrary code while the compiler is still on the
     * stack and before anything is ever executed by the caller.
     * <p>
     * The source unit is driven directly rather than through a {@code CompilationUnit}, which is what
     * makes this safe: transformations are applied by a compilation unit's phase operations and there
     * is no compilation unit here. Local transformations additionally require a phase no earlier than
     * semantic analysis, which is past where this stops. Note that the parser plugin does its work in
     * {@code convert} rather than in {@code parse}, so both are needed before errors can be reported;
     * nothing is resolved, so an unknown class is not an error at this point.
     *
     * @param script the script to validate
     */
    public static void validateGroovyScript(final String script) {
        val sourceUnit = SourceUnit.create("casGroovyScriptValidation", script);
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        if (sourceUnit.getErrorCollector().hasErrors()) {
            throw new IllegalArgumentException("Groovy script has errors: " + sourceUnit.getErrorCollector().getErrors());
        }
    }

    /**
     * New groovy class loader.
     *
     * @return the groovy class loader
     */
    public static GroovyClassLoader newGroovyClassLoader() {
        return new GroovyClassLoader(ScriptingUtils.class.getClassLoader(), createCompilerConfiguration());
    }

    private @Nullable Class loadGroovyClass(final Resource groovyScript,
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

    private static <T> @Nullable T getGroovyResult(final Resource groovyScript,
                                                   final String methodName,
                                                   final Object[] args,
                                                   final Class<T> clazz,
                                                   final boolean failOnError) throws Throwable {
        try {
            val groovyObject = parseGroovyScript(groovyScript, failOnError);
            if (groovyObject == null) {
                LOGGER.error("Could not parse the Groovy script at [{}]", groovyScript);
                return null;
            }
            return executeGroovyScript(groovyObject, methodName, args, clazz, failOnError);
        } catch (final Throwable e) {
            if (failOnError) {
                throw e;
            }
            LoggingUtils.error(LOGGER, e);
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
     * Gets object instance from groovy resource.
     *
     * @param <T>             the type parameter
     * @param resource        the resource
     * @param constructorArgs the constructor args
     * @param args            the args
     * @param expectedType    the expected type
     * @return the object instance from groovy resource
     */
    public static <T> @Nullable T getObjectInstanceFromGroovyResource(final Resource resource,
                                                                      final Class[] constructorArgs,
                                                                      final Object[] args,
                                                                      final Class<T> expectedType) {
        try {
            if (resource == null) {
                LOGGER.debug("No groovy script is defined");
                return null;
            }
            val clazz = loadObjectClass(resource);
            LOGGER.trace("Preparing constructor arguments [{}] for resource [{}]", args, resource);
            val ctor = clazz.getDeclaredConstructor(constructorArgs);
            val result = ctor.newInstance(args);
            if (!expectedType.isAssignableFrom(result.getClass())) {
                throw new ClassCastException("Result [" + result
                    + " is of type " + result.getClass()
                    + " when we were expecting " + expectedType);
            }
            return (T) result;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    /**
     * Compiles the groovy class behind the given resource, reusing the previously compiled
     * class as long as the resource has not been modified since. Compiling a class on every
     * call is expensive and this method sits on request paths.
     */
    private static Class<?> loadObjectClass(final Resource resource) throws Exception {
        val cacheKey = resource.getURI().toASCIIString();
        var lastModified = -1L;
        try {
            lastModified = resource.lastModified();
        } catch (final Exception e) {
            LOGGER.trace("Unable to determine the last modified date of [{}]", cacheKey);
        }
        val cached = OBJECT_CLASSES.get(cacheKey);
        if (cached != null && cached.lastModified() == lastModified) {
            LOGGER.trace("Reusing compiled groovy class for [{}]", cacheKey);
            return cached.clazz();
        }
        try (val inputStream = resource.getInputStream();
             val classLoader = newGroovyClassLoader()) {
            val script = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            LOGGER.debug("Compiling groovy class from [{}]", cacheKey);
            val clazz = classLoader.parseClass(script);
            OBJECT_CLASSES.put(cacheKey, new CompiledGroovyClass(lastModified, clazz));
            return clazz;
        }
    }

    private record CompiledGroovyClass(long lastModified, Class<?> clazz) {
    }

    private static CompilerConfiguration createCompilerConfiguration() {
        val compilerConfiguration = new CompilerConfiguration();
        val isStaticCompilation = BooleanUtils.toBoolean(System.getProperty(ExecutableCompiledScriptFactory.SYSTEM_PROPERTY_GROOVY_COMPILE_STATIC));
        if (CasRuntimeHintsRegistrar.inNativeImage() || isStaticCompilation) {
            compilerConfiguration.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic.class));
        }
        val imports = new ImportCustomizer();
        imports.addStarImports(
            "java.time",
            "java.util",
            "java.util.function",
            "java.io",
            "java.math",
            "java.beans",
            "java.net",
            "java.nio",
            "java.nio.charset",
            "java.util.stream",

            "groovy.net",
            "groovy.json",
            "groovy.text",
            "groovy.util",
            "groovy.lang",
            "groovy.transform",

            "org.slf4j",

            "org.apache.http",
            "org.apache.http.util",
            "org.apache.http.client.methods",
            "org.apache.http.impl.client",

            "org.apache.commons.lang3",
            "org.apache.commons.text",
            "org.apache.commons.io",
            "org.apache.commons.io.output",
            "org.apache.commons.codec.binary",
            "org.apache.commons.codec.digest",

            "org.apereo.inspektr.common.web",

            "jakarta.servlet",
            "jakarta.servlet.http",

            "org.ldaptive",
            "org.jose4j.jwk",

            "org.springframework.context",
            "org.springframework.core",
            "org.springframework.core.io",
            "org.springframework.webflow",
            "org.springframework.webflow.execution",
            "org.springframework.webflow.action",

            "org.opensaml.core.xml",
            "org.opensaml.saml.metadata.resolver",
            "org.opensaml.saml.saml2.core",
            "org.opensaml.saml.saml2.binding",
            "org.opensaml.saml.metadata.resolver",
            "org.opensaml.saml.common",

            "com.fasterxml.jackson",
            "com.fasterxml.jackson.databind",
            "tools.jackson",
            "tools.jackson.databind",

            "org.apereo.cas",
            "org.apereo.cas.api",
            "org.apereo.cas.audit",
            "org.apereo.cas.authentication",
            "org.apereo.cas.authentication.services",
            "org.apereo.cas.authentication.credential",
            "org.apereo.cas.authentication.principal",
            "org.apereo.cas.configuration.support",
            "org.apereo.cas.util",
            "org.apereo.cas.util.model",
            "org.apereo.cas.web",
            "org.apereo.cas.web.support",
            "org.apereo.cas.authentication.mfa",
            "org.apereo.cas.services",
            "org.apereo.cas.heimdall",
            "org.apereo.cas.heimdall.authorizer",
            "org.apereo.cas.support.saml",
            "org.apereo.cas.support.saml.services"
        );

        compilerConfiguration.addCompilationCustomizers(imports);
        return compilerConfiguration;
    }
}
