package org.apereo.cas.memcached.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.SneakyThrows;
import lombok.val;

import java.net.URL;

/**
 * Kryo serializer for {@link URL}.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public class URLSerializer extends Serializer<URL> {
    @Override
    @SneakyThrows
    public URL read(final Kryo kryo, final Input input, final Class<URL> aClass) {
        val url = kryo.readObject(input, String.class);
        return new URL(url);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final URL url) {
        kryo.writeObject(output, url.toExternalForm());
    }

}
