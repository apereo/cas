package org.apereo.cas.memcached.kryo.serial;

import module java.base;
import org.apereo.cas.memcached.kryo.CasKryoPool;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link URLSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Memcached")
@EnabledIfListeningOnPort(port = 11211)
class URLSerializerTests {
    @Test
    void verifyTranscoderWorks() throws Throwable {
        val pool = new CasKryoPool();
        try (val kryo = pool.borrow()) {
            val output = new ByteBufferOutput(4096);
            kryo.writeObject(output, new URI("https://github.com").toURL());
            val inputStream = new ByteArrayInputStream(output.toBytes());
            assertNotNull(kryo.readObject(new Input(inputStream), URL.class));
        }
    }
}
