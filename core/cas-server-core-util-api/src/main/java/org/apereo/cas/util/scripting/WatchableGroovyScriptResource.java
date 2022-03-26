package org.apereo.cas.util.scripting;

import org.apereo.cas.util.ResourceUtils;
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
    private final transient Resource resource;

    private transient FileWatcherService watcherService;

    private transient GroovyObject groovyScript;

    public WatchableGroovyScriptResource(final Resource script, final boolean enableWatcher) {
        this.resource = script;
        if (ResourceUtils.doesResourceExist(script)) {
            if (ResourceUtils.isFile(script) && enableWatcher) {
                this.watcherService = FunctionUtils.doUnchecked(
                    () -> new FileWatcherService(script.getFile(),
                        Unchecked.consumer(file -> {
                            LOGGER.info("Reloading script at [{}]", file);
                            compileScriptResource(script);
                        })));
                this.watcherService.start(script.getFilename());
            }
            compileScriptResource(script);
        }
    }

    public WatchableGroovyScriptResource(final Resource script) {
        this(script, true);
    }

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

    @Override
    public <T> T execute(final Object[] args, final Class<T> clazz, final boolean failOnError) {
        return groovyScript != null
            ? ScriptingUtils.executeGroovyScript(this.groovyScript, args, clazz, failOnError)
            : null;
    }

    @Override
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
        return groovyScript != null
            ? ScriptingUtils.executeGroovyScript(groovyScript, methodName, args, clazz, failOnError)
            : null;
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
