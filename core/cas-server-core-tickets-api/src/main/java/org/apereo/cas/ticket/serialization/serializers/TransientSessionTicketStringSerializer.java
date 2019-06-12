package org.apereo.cas.ticket.serialization.serializers;

import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

/**
 * This is {@link TransientSessionTicketStringSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class TransientSessionTicketStringSerializer extends AbstractJacksonBackedStringSerializer<TransientSessionTicketImpl> {
    private static final long serialVersionUID = 8959617299162115085L;

    public TransientSessionTicketStringSerializer() {
        super(MINIMAL_PRETTY_PRINTER);
    }

    @Override
    public Class<TransientSessionTicketImpl> getTypeToSerialize() {
        return TransientSessionTicketImpl.class;
    }
}
