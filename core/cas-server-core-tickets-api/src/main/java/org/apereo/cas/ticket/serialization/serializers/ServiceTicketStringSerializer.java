package org.apereo.cas.ticket.serialization.serializers;

import module java.base;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.util.serialization.BaseJacksonSerializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link ServiceTicketStringSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class ServiceTicketStringSerializer extends BaseJacksonSerializer<ServiceTicketImpl> {
    @Serial
    private static final long serialVersionUID = 8959617299162115085L;

    public ServiceTicketStringSerializer(final ConfigurableApplicationContext applicationContext) {
        super(MINIMAL_PRETTY_PRINTER, applicationContext, ServiceTicketImpl.class);
    }
}
