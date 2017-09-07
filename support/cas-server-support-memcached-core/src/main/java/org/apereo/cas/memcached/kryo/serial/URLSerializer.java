package org.apereo.cas.memcached.kryo.serial;

import java.net.MalformedURLException;
import java.net.URL;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Kryo serializer for {@link URL}.
 * 
 * @author Jerome Leleu
 * @since 4.0.0
 */
public class URLSerializer extends Serializer<URL> {

    @Override
    public URL read(final Kryo kryo, final Input input, final Class<URL> type) {
        final String url = kryo.readObject(input, String.class);
        try {
            return new URL(url);
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void write(final Kryo kryo, final Output output, final URL url) {
        kryo.writeObject(output, url.toExternalForm());
    }
}
