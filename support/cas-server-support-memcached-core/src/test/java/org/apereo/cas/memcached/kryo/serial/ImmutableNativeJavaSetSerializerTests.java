package org.apereo.cas.memcached.kryo.serial;

import org.apereo.cas.memcached.kryo.CasKryoPool;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.google.common.collect.ImmutableSet;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ImmutableNativeJavaSetSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Memcached")
@EnabledIfPortOpen(port = 11211)
public class ImmutableNativeJavaSetSerializerTests {
    @Test
    public void verifyTranscoderWorks() {
        val pool = new CasKryoPool();
        try (val kryo = pool.borrow()) {
            val output = new ByteBufferOutput(4096);
            kryo.writeObject(output, ImmutableSet.of("one"));

            val inputStream = new ByteArrayInputStream(output.toBytes());
            assertNotNull(kryo.readObject(new Input(inputStream), ImmutableSet.class));
        }
    }
}
