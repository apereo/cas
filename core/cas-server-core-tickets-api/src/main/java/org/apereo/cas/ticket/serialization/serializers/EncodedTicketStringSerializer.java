package org.apereo.cas.ticket.serialization.serializers;

import org.apereo.cas.ticket.registry.DefaultEncodedTicket;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

/**
 * This is {@link EncodedTicketStringSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class EncodedTicketStringSerializer extends AbstractJacksonBackedStringSerializer<DefaultEncodedTicket> {
    private static final long serialVersionUID = 8959835299162115085L;

    public EncodedTicketStringSerializer() {
        super(MINIMAL_PRETTY_PRINTER);
    }

    @Override
    public Class<DefaultEncodedTicket> getTypeToSerialize() {
        return DefaultEncodedTicket.class;
    }

}
