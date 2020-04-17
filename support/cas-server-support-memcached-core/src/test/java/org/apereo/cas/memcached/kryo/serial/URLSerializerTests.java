package org.apereo.cas.memcached.kryo.serial;

import org.apereo.cas.memcached.kryo.CasKryoPool;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link URLSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Memcached")
@EnabledIfPortOpen(port = 11211)
public class URLSerializerTests {
    @Test
    public void verifyTranscoderWorks() throws Exception {
        val pool = new CasKryoPool();
        try (val kryo = pool.borrow()) {
            val output = new ByteBufferOutput(4096);
            kryo.writeObject(output, new URL("https://github.com"));
            val inputStream = new ByteArrayInputStream(output.toBytes());
            assertNotNull(kryo.readObject(new Input(inputStream), URL.class));
        }
    }
}
