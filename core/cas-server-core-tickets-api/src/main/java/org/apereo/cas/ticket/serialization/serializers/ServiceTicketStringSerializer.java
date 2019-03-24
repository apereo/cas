package org.apereo.cas.ticket.serialization.serializers;

import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

/**
 * This is {@link ServiceTicketStringSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class ServiceTicketStringSerializer extends AbstractJacksonBackedStringSerializer<ServiceTicketImpl> {
    private static final long serialVersionUID = 8959617299162115085L;

    public ServiceTicketStringSerializer() {
        super(MINIMAL_PRETTY_PRINTER);
    }

    @Override
    public Class<ServiceTicketImpl> getTypeToSerialize() {
        return ServiceTicketImpl.class;
    }
}
