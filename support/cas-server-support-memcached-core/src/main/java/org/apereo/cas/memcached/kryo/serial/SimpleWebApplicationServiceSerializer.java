package org.apereo.cas.memcached.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;

/**
 * Serializer for {@link SimpleWebApplicationServiceImpl} class.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class SimpleWebApplicationServiceSerializer extends Serializer<SimpleWebApplicationServiceImpl> {

    @Override
    public void write(final Kryo kryo, final Output output, final SimpleWebApplicationServiceImpl service) {
        kryo.writeObject(output, service.getId());
    }

    @Override
    public SimpleWebApplicationServiceImpl read(final Kryo kryo, final Input input, final Class<SimpleWebApplicationServiceImpl> type) {
        final String id = kryo.readObject(input, String.class);
        return new WebApplicationServiceFactory().createService(id, SimpleWebApplicationServiceImpl.class);
    }
}
