package org.apereo.cas.util.scripting;

import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.io.FileWatcherService;

import groovy.lang.GroovyObject;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

/**
 * This is {@link WatchableGroovyScriptResource}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Getter
@ToString(of = "resource")
public class WatchableGroovyScriptResource implements AutoCloseable, ExecutableCompiledGroovyScript {
    private transient FileWatcherService watcherService;
    private transient GroovyObject groovyScript;
    private final transient Resource resource;

    @SneakyThrows
    public WatchableGroovyScriptResource(final Resource script) {
        this.resource = script;

        if (ResourceUtils.doesResourceExist(script)) {
            if (ResourceUtils.isFile(script)) {
                this.watcherService = new FileWatcherService(script.getFile(), file -> {
                    try {
                        LOGGER.debug("Reloading script at [{}]", file);
                        compileScriptResource(script);
                    } catch (final Exception e) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.error(e.getMessage(), e);
                        } else {
                            LOGGER.error(e.getMessage());
                        }
                    }
                });
                this.watcherService.start(script.getFilename());
                compileScriptResource(script);
            }
        }
    }


    private void compileScriptResource(final Resource script) {
        this.groovyScript = ScriptingUtils.parseGroovyScript(script, true);
    }

    /**
     * Execute.
     *
     * @param <T>   the type parameter
     * @param args  the args
     * @param clazz the clazz
     * @return the result
     */
    @Override
    public <T> T execute(final Object[] args, final Class<T> clazz) {
        return execute(args, clazz, true);
    }

    /**
     * Execute.
     *
     * @param args the args
     */
    @Override
    public void execute(final Object[] args) {
        execute(args, Void.class, true);
    }

    /**
     * Execute.
     *
     * @param <T>         the type parameter
     * @param args        the args
     * @param clazz       the clazz
     * @param failOnError the fail on error
     * @return the t
     */
    @Override
    public <T> T execute(final Object[] args, final Class<T> clazz, final boolean failOnError) {
        if (this.groovyScript != null) {
            return ScriptingUtils.executeGroovyScript(this.groovyScript, args, clazz, failOnError);
        }
        return null;
    }

    /**
     * Execute t.
     *
     * @param <T>        the type parameter
     * @param methodName the method name
     * @param clazz      the clazz
     * @param args       the args
     * @return the t
     */
    public <T> T execute(final String methodName, final Class<T> clazz, final Object... args) {
        return execute(methodName, clazz, true, args);
    }

    /**
     * Execute.
     *
     * @param <T>         the type parameter
     * @param methodName  the method name
     * @param clazz       the clazz
     * @param failOnError the fail on error
     * @param args        the args
     * @return the t
     */
    public <T> T execute(final String methodName, final Class<T> clazz, final boolean failOnError, final Object... args) {
        if (this.groovyScript != null) {
            return ScriptingUtils.executeGroovyScript(this.groovyScript, methodName, args, clazz, failOnError);
        }
        return null;
    }

    @Override
    public void close() {
        if (watcherService != null) {
            this.watcherService.close();
        }
    }
}
