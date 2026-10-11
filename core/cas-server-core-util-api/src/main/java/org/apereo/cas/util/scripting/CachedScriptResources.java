package org.apereo.cas.util.scripting;

import module java.base;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;

/**
 * Resolves script resources through the shared {@link ScriptResourceCacheManager}, so that
 * every caller of the same location shares one compiled script and one file watcher instead of
 * compiling and watching the script again for each deserialized object.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
@UtilityClass
public class CachedScriptResources {
    private static final CasReentrantLock LOCK = new CasReentrantLock();

    /**
     * Resolve the script backed by the given resource, failing on errors.
     *
     * @param resource the resource
     * @return the executable compiled script
     */
    public static ExecutableCompiledScript fromResource(final Resource resource) {
        return fromResource(resource, true);
    }

    /**
     * Resolve the script backed by the given resource.
     * A resource that does not exist is not cached, so it is picked up once it appears.
     * Without a cache manager, the script is compiled without a file watcher.
     *
     * @param resource    the resource
     * @param failOnError whether script failures are reported to the caller
     * @return the executable compiled script
     */
    public static ExecutableCompiledScript fromResource(final Resource resource, final boolean failOnError) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        val cacheManager = ApplicationContextProvider.getScriptResourceCacheManager();
        if (cacheManager.isEmpty() || !ResourceUtils.doesResourceExist(resource)) {
            return scriptFactory.fromResource(resource, false).setFailOnError(failOnError);
        }
        val manager = cacheManager.get();
        val key = manager.computeKey(resource.getDescription(), String.valueOf(failOnError));
        return LOCK.execute(() -> {
            val cached = manager.get(key);
            if (cached != null) {
                return cached;
            }
            LOGGER.trace("Caching script [{}] under key [{}]", resource, key);
            val script = scriptFactory.fromResource(resource).setFailOnError(failOnError);
            manager.put(key, script);
            return script;
        });
    }
}
