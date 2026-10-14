package org.apereo.cas.util.scripting;

import module java.base;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import groovy.lang.GroovyObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
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
@Accessors(chain = true)
public class WatchableGroovyScriptResource implements ExecutableCompiledScript {
    private final CasReentrantLock lock = new CasReentrantLock();

    private final Resource resource;

    @Nullable
    private FileWatcherService watcherService;

    @Nullable
    private volatile CompiledGroovyScript<GroovyObject> compiledScript;

    @Setter
    private boolean failOnError = true;

    private volatile boolean closed;

    public WatchableGroovyScriptResource(final Resource script, final boolean enableWatcher) {
        this.resource = script;
        if (ResourceUtils.doesResourceExist(script)) {
            if (ResourceUtils.isFile(script) && enableWatcher) {
                watcherService = FunctionUtils.doUnchecked(
                    () -> new FileWatcherService(script.getFile(),
                        file -> {
                            LOGGER.debug("Reloading script at [{}]", file);
                            compileScriptResource(script);
                            LOGGER.info("Reloaded script at [{}]", file);
                        }));
                watcherService.start(script.getFilename());
            }
            compileScriptResource(script);
        }
    }

    @Override
    public <T> @Nullable T execute(final Object[] args, final Class<T> clazz) throws Throwable {
        return execute(args, clazz, failOnError);
    }

    @Override
    public void execute(final Object[] args) throws Throwable {
        execute(args, Void.class, failOnError);
    }

    @Override
    public <T> @Nullable T execute(final String methodName, final Class<T> clazz, final Object... args) throws Throwable {
        return execute(methodName, clazz, failOnError, args);
    }

    @Override
    public <T> @Nullable T execute(final Object[] args, final Class<T> clazz, final boolean failOnError) {
        return execute("run", clazz, failOnError, args);
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
    public <T> @Nullable T execute(final String methodName, final Class<T> clazz, final boolean failOnError,
                                   final Object... args) {
        val compiled = compiledScript;
        if (compiled == null) {
            return null;
        }
        val sharedInstance = compiled.sharedInstance();
        if (sharedInstance != null) {
            return lock.executeAndThrow(() -> executeScript(sharedInstance, methodName, clazz, failOnError, args));
        }
        try {
            val groovyObject = compiled.newInstance(failOnError);
            return groovyObject != null ? executeScript(groovyObject, methodName, clazz, failOnError, args) : null;
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        closed = true;
        if (watcherService != null) {
            LOGGER.trace("Shutting down watcher service for [{}]", this.resource);
            this.watcherService.close();
        }
    }

    private <T> @Nullable T executeScript(final GroovyObject groovyObject, final String methodName,
                                          final Class<T> clazz, final boolean failOnError,
                                          final Object... args)
            throws Throwable {
        try {
            LOGGER.trace("Beginning to execute script [{}]", this);
            return ScriptingUtils.executeGroovyScript(groovyObject, methodName, args, clazz, failOnError);
        } finally {
            LOGGER.trace("Completed script execution [{}]", this);
        }
    }

    private void compileScriptResource(final Resource script) {
        this.compiledScript = ScriptingUtils.compileGroovyScript(script, failOnError);
    }
}
