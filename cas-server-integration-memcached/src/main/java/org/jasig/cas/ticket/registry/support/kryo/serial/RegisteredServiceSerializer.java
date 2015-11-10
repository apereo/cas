package org.jasig.cas.ticket.registry.support.kryo.serial;

import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializier for {@link org.jasig.cas.services.RegisteredService} instances.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class RegisteredServiceSerializer  extends Serializer<RegisteredService> {
    
    @Override
    public void write(final Kryo kryo, final Output output, final RegisteredService service) {
        kryo.writeObject(output, service.getServiceId());
    }

    @Override
    public RegisteredService read(final Kryo kryo, final Input input, final Class<RegisteredService> type) {
        final String id = kryo.readObject(input, String.class);
        final AbstractRegisteredService svc = new RegisteredServiceImpl();
        svc.setServiceId(id);
        return svc;
    }
}
