package org.apereo.cas.ticket.serialization.serializers;

import org.apereo.cas.ticket.registry.EncodedTicket;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

/**
 * This is {@link EncodedTicketStringSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class EncodedTicketStringSerializer extends AbstractJacksonBackedStringSerializer<EncodedTicket> {
    private static final long serialVersionUID = 8959835299162115085L;

    public EncodedTicketStringSerializer() {
        super(MINIMAL_PRETTY_PRINTER);
    }

    @Override
    public Class<EncodedTicket> getTypeToSerialize() {
        return EncodedTicket.class;
    }

}
