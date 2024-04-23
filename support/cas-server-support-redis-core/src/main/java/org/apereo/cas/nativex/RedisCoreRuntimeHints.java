package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import com.redis.lettucemod.api.sync.RedisModulesCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import java.util.List;

/**
 * This is {@link RedisCoreRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class RedisCoreRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerChainedProxyHints(hints, RedisModulesCommands.class, RedisClusterCommands.class);
        registerProxyHints(hints, RedisConnectionFactory.class);
        registerReflectionHints(hints, List.of(
            RedisModulesCommands.class,
            RedisClusterCommands.class,
            RedisConnectionFactory.class));
    }
}



