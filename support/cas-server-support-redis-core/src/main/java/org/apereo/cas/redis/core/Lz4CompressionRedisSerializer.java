package org.apereo.cas.redis.core;

import lombok.val;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * This is {@link Lz4CompressionRedisSerializer}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class Lz4CompressionRedisSerializer<T> implements RedisSerializer<T> {
    /**
     * Bytes reserved to store original length.
     */
    private static final int HEADER_LENGTH = Integer.BYTES;

    private final RedisSerializer<T> delegate;
    private final LZ4Compressor compressor;
    private final LZ4FastDecompressor decompressor;

    public Lz4CompressionRedisSerializer(final RedisSerializer<T> delegate) {
        this.delegate = delegate;
        val factory = LZ4Factory.fastestInstance();
        this.compressor = factory.highCompressor();
        this.decompressor = factory.fastDecompressor();
    }

    @Override
    public byte[] serialize(final T value) throws SerializationException {
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
    public T deserialize(final byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= HEADER_LENGTH) {
            return delegate.deserialize(bytes);
        }
        val buffer = ByteBuffer.wrap(bytes);
        val originalLength = buffer.getInt();
        val restored = new byte[originalLength];
        decompressor.decompress(
            bytes, HEADER_LENGTH,
            restored, 0, originalLength
        );
        return delegate.deserialize(restored);
    }
}
