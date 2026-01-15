package org.apereo.cas.memcached.kryo.serial;

import module java.base;
import org.apereo.cas.memcached.kryo.CasKryoPool;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * This is {@link ZonedDateTimeSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Memcached")
@EnabledIfListeningOnPort(port = 11211)
class ZonedDateTimeSerializerTests {

    @Test
    void verifyTranscoderWorks() {
        val pool = new CasKryoPool();
        try (val kryo = pool.borrow()) {
            val output = new ByteBufferOutput(2048);
            kryo.writeObject(output, ZonedDateTime.now(ZoneOffset.UTC));
            kryo.writeObject(output, ZonedDateTime.now(ZoneId.systemDefault()));
        }
    }
}
