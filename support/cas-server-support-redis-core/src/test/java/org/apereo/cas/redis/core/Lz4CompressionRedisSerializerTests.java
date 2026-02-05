package org.apereo.cas.redis.core;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link Lz4CompressionRedisSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("Redis")
class Lz4CompressionRedisSerializerTests {

    @Test
    void verifyOperation() throws Exception {
        val stringRedisSerializer = new StringRedisSerializer();
        val serializer = new Lz4CompressionRedisSerializer(stringRedisSerializer);
        val original = "This is a test string for LZ4 compression serializer in Redis.";
        val serialized = serializer.serialize(original);
        assertNotNull(serialized);
        val deserialized = serializer.deserialize(serialized);
        assertEquals(original, deserialized);
    }

    @Test
    void verifyMismatchOperation() {
        val serializer = new Lz4CompressionRedisSerializer(new StringRedisSerializer());
        val notCompressed = new byte[] {
            (byte) 0xAD,
            (byte) 0xB2,
            (byte) 0xC3,
            (byte) 0x45,
            (byte) 0x10,
            (byte) 0x20
        };
        val deserialized = serializer.deserialize(notCompressed);
        assertNotNull(deserialized);
    }
}
