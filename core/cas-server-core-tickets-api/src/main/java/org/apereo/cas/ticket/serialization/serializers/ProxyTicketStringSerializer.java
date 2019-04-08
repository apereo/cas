package org.apereo.cas.ticket.serialization.serializers;

import org.apereo.cas.ticket.ProxyTicketImpl;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

/**
 * This is {@link ProxyTicketStringSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class ProxyTicketStringSerializer extends AbstractJacksonBackedStringSerializer<ProxyTicketImpl> {
    private static final long serialVersionUID = -6343596853082798477L;

    public ProxyTicketStringSerializer() {
        super(MINIMAL_PRETTY_PRINTER);
    }

    @Override
    public Class<ProxyTicketImpl> getTypeToSerialize() {
        return ProxyTicketImpl.class;
    }
}
