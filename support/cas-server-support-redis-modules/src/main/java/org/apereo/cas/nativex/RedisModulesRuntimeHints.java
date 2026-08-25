package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import io.lettuce.core.api.sync.RediSearchCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link RedisModulesRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class RedisModulesRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerChainedProxyHints(hints, RedisCommands.class, RedisClusterCommands.class);
        registerProxyHints(hints, RedisAdvancedClusterCommands.class);
        registerReflectionHints(hints, List.of(RediSearchCommands.class));
    }
}
