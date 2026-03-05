package org.apereo.cas.util.serialization;

import module java.base;
import lombok.val;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link LZ4CompressionHandler}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public class LZ4CompressionHandler {
    /**
     * Bytes reserved to store original length.
     */
    private static final int HEADER_LENGTH = Integer.BYTES;

    private final LZ4Compressor compressor;
    private final LZ4FastDecompressor decompressor;

    public LZ4CompressionHandler() {
        val factory = LZ4Factory.fastestInstance();
        this.compressor = factory.highCompressor();
        this.decompressor = factory.fastDecompressor();
    }

    /**
     * Compress byte [].
     *
     * @param raw the raw
     * @return the byte []
     */
    public byte @Nullable [] compress(final byte[] raw) {
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

    /**
     * Decompress byte[].
     *
     * @param bytes the bytes
     * @return the byte[]
     */
    public byte @Nullable [] decompress(final byte @Nullable [] bytes) {
        if (bytes == null || bytes.length <= HEADER_LENGTH) {
            return bytes;
        }
        val buffer = ByteBuffer.wrap(bytes);
        val originalLength = buffer.getInt();
        if (originalLength <= 0) {
            return bytes;
        }
        val restored = new byte[originalLength];
        decompressor.decompress(bytes, HEADER_LENGTH, restored, 0, originalLength);
        return restored;
    }
}
