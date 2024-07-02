package org.apereo.cas.util.scripting;

import org.springframework.core.io.Resource;
import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * This is {@link ExecutableCompiledScriptFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface ExecutableCompiledScriptFactory {

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
            .orElseThrow(() -> new IllegalArgumentException("""
                No executable compiled script factory is found.
                Examine your build and make sure you have a dependency on the CAS module that provides the factory implementation.
                """));
    }
}
