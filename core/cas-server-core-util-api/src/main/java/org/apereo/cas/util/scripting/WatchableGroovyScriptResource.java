package org.apereo.cas.util.scripting;

import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.io.FileWatcherService;

import groovy.lang.GroovyObject;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

/**
 * This is {@link WatchableGroovyScriptResource}.
 *
 * @author Misagh Moayyed
 * @since 5.3.7
 */
@Slf4j
@Getter
public class WatchableGroovyScriptResource {
    private final transient FileWatcherService watcherService;

    private final transient Resource resource;
    private transient GroovyObject groovyScript;

    @SneakyThrows
    public WatchableGroovyScriptResource(final Resource script) {
        this.resource = script;

        if (ResourceUtils.doesResourceExist(script)) {
            this.watcherService = new FileWatcherService(script.getFile(), file -> {
                try {
                    LOGGER.debug("Reloading script at [{}]", file);
                    compileScriptResource(script);
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            });
            this.watcherService.start(script.getFilename());
            compileScriptResource(script);
        } else {
            this.watcherService = null;
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
    public <T> T execute(final Object[] args, final Class<T> clazz) {
        if (this.groovyScript != null) {
            return ScriptingUtils.executeGroovyScript(this.groovyScript, args, clazz, true);
        }
        return null;
    }
}
