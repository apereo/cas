package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import com.redis.lettucemod.api.sync.RedisModulesCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link RedisCoreRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class RedisCoreRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.proxies()
            .registerJdkProxy(RedisModulesCommands.class, RedisClusterCommands.class);
    }
}
