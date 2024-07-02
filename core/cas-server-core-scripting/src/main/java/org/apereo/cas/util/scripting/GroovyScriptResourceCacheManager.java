package org.apereo.cas.util.scripting;

import org.apereo.cas.configuration.model.core.cache.ExpiringSimpleCacheProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.util.Set;

/**
 * This is {@link GroovyScriptResourceCacheManager}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class GroovyScriptResourceCacheManager implements ScriptResourceCacheManager<String, ExecutableCompiledScript> {
    private final CasReentrantLock lock = new CasReentrantLock();

    private final Cache<String, ExecutableCompiledScript> cache;

    public GroovyScriptResourceCacheManager(final ExpiringSimpleCacheProperties properties) {
        this.cache = Beans.newCacheBuilder(properties).build();
    }

    @Override
    public ExecutableCompiledScript get(final String key) {
        return lock.tryLock(() -> cache.getIfPresent(key));
    }

    @Override
    public boolean containsKey(final String key) {
        return get(key) != null;
    }

    @Override
    @CanIgnoreReturnValue
    public ScriptResourceCacheManager<String, ExecutableCompiledScript> put(
        final String key, final ExecutableCompiledScript value) {
        return lock.tryLock(() -> {
            this.cache.put(key, value);
            return this;
        });
    }

    @Override
    @CanIgnoreReturnValue
    public ScriptResourceCacheManager<String, ExecutableCompiledScript> remove(final String key) {
        return lock.tryLock(() -> {
            this.cache.invalidate(key);
            return this;
        });
    }

    @Override
    public Set<String> getKeys() {
        return lock.tryLock(() -> cache.asMap().keySet());
    }

    @Override
    public void close() {
        lock.tryLock(__ -> cache.invalidateAll());
    }

    @Override
    public boolean isEmpty() {
        return lock.tryLock(() -> cache.asMap().isEmpty());
    }

    @Override
    public ExecutableCompiledScript resolveScriptableResource(
        final String scriptResource,
        final String... keys) {

        val cacheKey = computeKey(keys);
        LOGGER.trace("Constructed cache key [{}] for keys [{}] mapped as groovy script", cacheKey, keys);

        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        
        var script = (ExecutableCompiledScript) null;
        if (containsKey(cacheKey)) {
            script = get(cacheKey);
            LOGGER.trace("Located cached groovy script [{}] for key [{}]", script, cacheKey);
        } else {
            try {
                if (ScriptingUtils.isExternalGroovyScript(scriptResource)) {
                    val scriptPath = SpringExpressionLanguageValueResolver.getInstance().resolve(scriptResource);
                    val resource = ResourceUtils.getResourceFrom(scriptPath);
                    script = scriptFactory.fromResource(resource);
                } else {
                    var resourceToUse = scriptResource;
                    if (ScriptingUtils.isInlineGroovyScript(resourceToUse)) {
                        val matcher = ScriptingUtils.getMatcherForInlineGroovyScript(resourceToUse);
                        if (matcher.find()) {
                            resourceToUse = matcher.group(1);
                        }
                    }
                    script = scriptFactory.fromScript(resourceToUse);
                }
                LOGGER.trace("Groovy script [{}] for key [{}] is not cached", scriptResource, cacheKey);
                put(cacheKey, script);
                LOGGER.trace("Cached groovy script [{}] for key [{}]", script, cacheKey);
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
        }
        return script;
    }

    @Override
    public String computeKey(final String... keys) {
        val rawKey = String.join(":", keys);
        return DigestUtils.sha256(rawKey);
    }
}
