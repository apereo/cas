package org.apereo.cas.ticket.serialization.serializers;

import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import java.io.Serial;

/**
 * This is {@link TicketGrantingTicketStringSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class TicketGrantingTicketStringSerializer extends AbstractJacksonBackedStringSerializer<TicketGrantingTicketImpl> {
    @Serial
    private static final long serialVersionUID = 1527874389457723545L;

    public TicketGrantingTicketStringSerializer() {
        super(MINIMAL_PRETTY_PRINTER);
    }

    @Override
    public Class<TicketGrantingTicketImpl> getTypeToSerialize() {
        return TicketGrantingTicketImpl.class;
    }
}
