package org.apereo.cas.ticket.serialization.serializers;

import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

/**
 * This is {@link ProxyGrantingTicketStringSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class ProxyGrantingTicketStringSerializer extends AbstractJacksonBackedStringSerializer<ProxyGrantingTicketImpl> {
    private static final long serialVersionUID = 7089208351327601379L;

    public ProxyGrantingTicketStringSerializer() {
        super(MINIMAL_PRETTY_PRINTER);
    }

    @Override
    public Class<ProxyGrantingTicketImpl> getTypeToSerialize() {
        return ProxyGrantingTicketImpl.class;
    }
}
