package org.apereo.cas.nativex;

import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.jpountz.lz4.LZ4SafeDecompressor;
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
        registerProxyHints(hints, RedisConnectionFactory.class);
        registerReflectionHints(hints, List.of(
            RedisClusterCommands.class,
            RedisConnectionFactory.class));
        registerReflectionHints(hints, findSubclassesInPackage(LZ4Compressor.class, LZ4Compressor.class.getPackageName()));
        registerReflectionHints(hints, findSubclassesInPackage(LZ4FastDecompressor.class, LZ4FastDecompressor.class.getPackageName()));
        registerReflectionHints(hints, findSubclassesInPackage(LZ4SafeDecompressor.class, LZ4SafeDecompressor.class.getPackageName()));
    }
}



