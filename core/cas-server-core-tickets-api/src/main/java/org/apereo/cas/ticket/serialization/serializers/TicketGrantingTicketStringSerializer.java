package org.apereo.cas.ticket.serialization.serializers;

import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.util.serialization.AbstractJacksonBackedNonHjsonStringSerializer;

import org.springframework.context.ConfigurableApplicationContext;
import java.io.Serial;

/**
 * This is {@link TicketGrantingTicketStringSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class TicketGrantingTicketStringSerializer extends AbstractJacksonBackedNonHjsonStringSerializer<TicketGrantingTicketImpl> {
    @Serial
    private static final long serialVersionUID = 1527874389457723545L;

    public TicketGrantingTicketStringSerializer(final ConfigurableApplicationContext applicationContext) {
        super(MINIMAL_PRETTY_PRINTER, applicationContext);
    }

    @Override
    public Class<TicketGrantingTicketImpl> getTypeToSerialize() {
        return TicketGrantingTicketImpl.class;
    }
}
