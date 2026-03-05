package org.apereo.cas.redis.core;

import module java.base;
import org.apereo.cas.util.serialization.LZ4CompressionHandler;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * This is {@link LZ4CompressionRedisSerializer}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class LZ4CompressionRedisSerializer<T> implements RedisSerializer<@NonNull T> {
    private final RedisSerializer<@NonNull T> delegate;
    private final LZ4CompressionHandler compressionHandler;

    @Override
    public byte @NonNull [] serialize(final T value) throws SerializationException {
        val raw = delegate.serialize(value);
        return compressionHandler.compress(raw);
    }

    @Override
    public T deserialize(final byte @NonNull [] bytes) throws SerializationException {
        val result = compressionHandler.decompress(bytes);
        return delegate.deserialize(result);
    }
}
