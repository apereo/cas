package org.apereo.cas.redis.core;

import module java.base;
import lombok.val;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * This is {@link Lz4CompressionRedisSerializer}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class Lz4CompressionRedisSerializer<T> implements RedisSerializer<@NonNull T> {
    /**
     * Bytes reserved to store original length.
     */
    private static final int HEADER_LENGTH = Integer.BYTES;

    private final RedisSerializer<@NonNull T> delegate;
    private final LZ4Compressor compressor;
    private final LZ4FastDecompressor decompressor;

    public Lz4CompressionRedisSerializer(final RedisSerializer<@NonNull T> delegate) {
        this.delegate = delegate;
        val factory = LZ4Factory.fastestInstance();
        this.compressor = factory.highCompressor();
        this.decompressor = factory.fastDecompressor();
    }

    @Override
    public byte @NonNull [] serialize(final T value) throws SerializationException {
        val raw = delegate.serialize(value);
        if (raw == null || raw.length == 0) {
            return raw;
        }
        val maxCompressedLength = compressor.maxCompressedLength(raw.length);
        val compressed = new byte[HEADER_LENGTH + maxCompressedLength];
        ByteBuffer.wrap(compressed).putInt(raw.length);
        val compressedLength = compressor.compress(
            raw, 0, raw.length,
            compressed, HEADER_LENGTH, maxCompressedLength
        );
        return Arrays.copyOf(compressed, 4 + compressedLength);
    }

    @Override
    public T deserialize(final byte @NonNull [] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= HEADER_LENGTH) {
            return delegate.deserialize(bytes);
        }
        val buffer = ByteBuffer.wrap(bytes);
        val originalLength = buffer.getInt();
        if (originalLength <= 0) {
            return delegate.deserialize(bytes);
        }
        val restored = new byte[originalLength];
        decompressor.decompress(
            bytes, HEADER_LENGTH,
            restored, 0, originalLength
        );
        return delegate.deserialize(restored);
    }
}
