package org.apereo.cas.memcached.kryo;

import com.esotericsoftware.kryo.io.ByteBufferOutput;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * This is {@link ZonedDateTimeSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ZonedDateTimeSerializerTests {

    @Test
    public void verifyTranscoderWorks() {
        val pool = new CasKryoPool();
        try (val kryo = pool.borrow()) {
            val output = new ByteBufferOutput(2048);
            kryo.writeObject(output, ZonedDateTime.now(ZoneOffset.UTC));
            kryo.writeObject(output, ZonedDateTime.now());
        }
    }
}
