package org.apereo.cas.util.scripting;

import module java.base;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;

/**
 * This is {@link ExecutableCompiledScriptFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface ExecutableCompiledScriptFactory {
    /**
     * System property to indicate groovy compilation must be static.
     */
    String SYSTEM_PROPERTY_GROOVY_COMPILE_STATIC = "org.apereo.cas.groovy.compile.static";

    /**
     * From resource executable compiled script.
     *
     * @param resource      the resource
     * @param watchResource the watch resource
     * @return the executable compiled script
     */
    ExecutableCompiledScript fromResource(Resource resource, boolean watchResource);

    /**
     * From resource executable compiled script.
     *
     * @param resource the resource
     * @return the executable compiled script
     */
    default ExecutableCompiledScript fromResource(final Resource resource) {
        return fromResource(resource, true);
    }

    /**
     * From shell script.
     *
     * @param script the script
     * @return the executable compiled script
     */
    ExecutableCompiledScript fromScript(String script);
    
    /**
     * Create template string.
     *
     * @param contents       the contents
     * @param templateParams the template params
     * @return the string
     * @throws Exception the exception
     */
    String createTemplate(String contents, Map<String, ?> templateParams) throws Exception;

    /**
     * Create template string.
     *
     * @param contents       the contents
     * @param templateParams the template params
     * @return the string
     * @throws Exception the exception
     */
    String createTemplate(File contents, Map<String, ?> templateParams) throws Exception;

    /**
     * New object instance t.
     *
     * @param <T>            the type parameter
     * @param script         the script
     * @param clazz the predicate class
     * @return the t
     * @throws Exception the exception
     */
    <T> T newObjectInstance(String script, Class<T> clazz) throws Exception;

    /**
     * New object instance t.
     *
     * @param <T>            the type parameter
     * @param resource       the resource
     * @param ctorParameters the ctor parameters
     * @param args           the args
     * @param clazz          the clazz
     * @return the t
     */
    <T> @Nullable T newObjectInstance(Resource resource, Class[] ctorParameters,
                                      Object[] args, Class<T> clazz);
    

    /**
     * Find executable compiled script factory.
     *
     * @return the optional
     */
    static Optional<ExecutableCompiledScriptFactory> findExecutableCompiledScriptFactory() {
        return ServiceLoader.load(ExecutableCompiledScriptFactory.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .findFirst();
    }

    /**
     * Gets executable compiled script factory.
     *
     * @return the executable compiled script factory
     */
    static ExecutableCompiledScriptFactory getExecutableCompiledScriptFactory() {
        return findExecutableCompiledScriptFactory()
            .orElseThrow(() -> new IllegalArgumentException("No executable compiled script factory is found. "
                + "Your CAS server is not configured to support scripting. "
                + "Examine your build and make sure you have included the CAS dependency/module that provides the script factory implementation."));
    }

    /**
     * Is inline script boolean.
     *
     * @param input the input
     * @return true or false
     */
    default boolean isInlineScript(final String input) {
        return getInlineScript(input).isPresent();
    }

    /**
     * Is external script boolean.
     *
     * @param input the input
     * @return true or false
     */
    default boolean isExternalScript(final String input) {
        return getExternalScript(input).isPresent();
    }

    /**
     * Is script?.
     *
     * @param text the text
     * @return true/false
     */
    default boolean isScript(final String text) {
        return isExternalScript(text) || isInlineScript(text);
    }
    
    /**
     * Gets inline script.
     *
     * @param input the input
     * @return the inline script
     */
    Optional<String> getInlineScript(String input);

    /**
     * Gets external script.
     *
     * @param input the input
     * @return the external script
     */
    Optional<String> getExternalScript(String input);

    /**
     * New class loader.
     *
     * @return the class loader
     */
    URLClassLoader newClassLoader();
}
