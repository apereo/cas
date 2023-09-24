package org.apereo.cas.util.scripting;

import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import groovy.lang.GroovyObject;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
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
public class WatchableGroovyScriptResource implements ExecutableCompiledGroovyScript {
    private final CasReentrantLock lock = new CasReentrantLock();

    private final Resource resource;

    private FileWatcherService watcherService;

    private GroovyObject groovyScript;

    public WatchableGroovyScriptResource(final Resource script, final boolean enableWatcher) {
        this.resource = script;
        if (ResourceUtils.doesResourceExist(script)) {
            if (ResourceUtils.isFile(script) && enableWatcher) {
                watcherService = FunctionUtils.doUnchecked(
                    () -> new FileWatcherService(script.getFile(),
                        Unchecked.consumer(file -> {
                            LOGGER.info("Reloading script at [{}]", file);
                            compileScriptResource(script);
                        })));
                watcherService.start(script.getFilename());
            }
            compileScriptResource(script);
        }
    }

    public WatchableGroovyScriptResource(final Resource script) {
        this(script, true);
    }

    @Override
    public <T> T execute(final Object[] args, final Class<T> clazz) throws Throwable {
        return execute(args, clazz, true);
    }

    @Override
    public void execute(final Object[] args) throws Throwable {
        execute(args, Void.class, true);
    }

    @Override
    public <T> T execute(final String methodName, final Class<T> clazz, final Object... args) throws Throwable {
        return execute(methodName, clazz, true, args);
    }

    @Override
    public <T> T execute(final Object[] args, final Class<T> clazz, final boolean failOnError) throws Throwable {
        return lock.tryLock(() -> {
            try {
                LOGGER.trace("Beginning to execute script [{}]", this);
                return groovyScript != null
                    ? ScriptingUtils.executeGroovyScript(this.groovyScript, args, clazz, failOnError)
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
    public <T> T execute(final String methodName, final Class<T> clazz, final boolean failOnError,
                         final Object... args) throws Throwable {
        return lock.tryLock(() -> {
            try {
                LOGGER.trace("Beginning to execute script [{}]", this);
                return groovyScript != null
                    ? ScriptingUtils.executeGroovyScript(groovyScript, methodName, args, clazz, failOnError)
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
        this.groovyScript = ScriptingUtils.parseGroovyScript(script, true);
    }
}
