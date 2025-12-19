package org.apereo.cas.util.scripting;

import module java.base;
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
    private GroovyObject compiledScript;

    @Setter
    private boolean failOnError = true;

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

    public WatchableGroovyScriptResource(final Resource script) {
        this(script, true);
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
        return lock.tryLock(() -> {
            try {
                LOGGER.trace("Beginning to execute script [{}]", this);
                return compiledScript != null
                    ? ScriptingUtils.executeGroovyScript(this.compiledScript, args, clazz, failOnError)
                    : null;
            } finally {
                LOGGER.trace("Completed script execution [{}]", this);
            }
        });
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
        return lock.tryLock(() -> {
            try {
                LOGGER.trace("Beginning to execute script [{}]", this);
                return compiledScript != null
                    ? ScriptingUtils.executeGroovyScript(compiledScript, methodName, args, clazz, failOnError)
                    : null;
            } finally {
                LOGGER.trace("Completed script execution [{}]", this);
            }
        });
    }
    
    @Override
    public void close() {
        if (watcherService != null) {
            LOGGER.trace("Shutting down watcher service for [{}]", this.resource);
            this.watcherService.close();
        }
    }

    private void compileScriptResource(final Resource script) {
        this.compiledScript = ScriptingUtils.parseGroovyScript(script, failOnError);
    }
}
