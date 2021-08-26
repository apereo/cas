package org.apereo.cas.util.scripting;

import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;

/**
 * This is {@link GroovyScriptResourceCacheManager}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class GroovyScriptResourceCacheManager implements ScriptResourceCacheManager<String, ExecutableCompiledGroovyScript> {
    private static final Duration EXPIRATION_AFTER_ACCESS = Duration.ofHours(8);

    private final Cache<String, ExecutableCompiledGroovyScript> cache;

    public GroovyScriptResourceCacheManager() {
        this.cache = Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(1000)
            .expireAfterAccess(EXPIRATION_AFTER_ACCESS)
            .removalListener((RemovalListener<String, ExecutableCompiledGroovyScript>) (key, value, cause) -> {
                LOGGER.trace("Removing script [{}] from cache under [{}]; removal cause is [{}]", value, key, cause);
                Objects.requireNonNull(value).close();
            })
            .build();
    }

    @Override
    public ExecutableCompiledGroovyScript get(final String key) {
        return this.cache.getIfPresent(key);
    }

    @Override
    public boolean containsKey(final String key) {
        return get(key) != null;
    }

    @Override
    public ScriptResourceCacheManager<String, ExecutableCompiledGroovyScript> put(final String key,
                                                                                  final ExecutableCompiledGroovyScript value) {
        this.cache.put(key, value);
        return this;
    }

    @Override
    public ScriptResourceCacheManager<String, ExecutableCompiledGroovyScript> remove(final String key) {
        this.cache.invalidate(key);
        return this;
    }

    @Override
    public void close() {
        cache.invalidateAll();
    }

    @Override
    public boolean isEmpty() {
        return cache.asMap().isEmpty();
    }

    @Override
    public ExecutableCompiledGroovyScript resolveScriptableResource(
        final String scriptResource,
        final String... keys) {

        val cacheKey = ScriptResourceCacheManager.computeKey(keys);
        LOGGER.trace("Constructed cache key [{}] for keys [{}] mapped as groovy script", cacheKey, keys);
        var script = (ExecutableCompiledGroovyScript) null;
        if (containsKey(cacheKey)) {
            script = get(cacheKey);
            LOGGER.trace("Located cached groovy script [{}] for key [{}]", script, cacheKey);
        } else {
            try {
                if (ScriptingUtils.isExternalGroovyScript(scriptResource)) {
                    val scriptPath = SpringExpressionLanguageValueResolver.getInstance().resolve(scriptResource);
                    val resource = ResourceUtils.getResourceFrom(scriptPath);
                    script = new WatchableGroovyScriptResource(resource);
                } else {
                    script = new GroovyShellScript(scriptResource);
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
    public Set<String> getKeys() {
        return this.cache.asMap().keySet();
    }
}
