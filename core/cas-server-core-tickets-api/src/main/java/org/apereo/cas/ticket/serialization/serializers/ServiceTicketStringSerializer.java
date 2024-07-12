package org.apereo.cas.ticket.serialization.serializers;

import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import org.springframework.context.ConfigurableApplicationContext;
import java.io.Serial;

/**
 * This is {@link ServiceTicketStringSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class ServiceTicketStringSerializer extends AbstractJacksonBackedStringSerializer<ServiceTicketImpl> {
    @Serial
    private static final long serialVersionUID = 8959617299162115085L;

    public ServiceTicketStringSerializer(final ConfigurableApplicationContext applicationContext) {
        super(MINIMAL_PRETTY_PRINTER, applicationContext);
    }

    @Override
    public Class<ServiceTicketImpl> getTypeToSerialize() {
        return ServiceTicketImpl.class;
    }
}
