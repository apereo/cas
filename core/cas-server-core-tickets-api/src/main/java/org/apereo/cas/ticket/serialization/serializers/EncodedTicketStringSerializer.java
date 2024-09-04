package org.apereo.cas.ticket.serialization.serializers;

import org.apereo.cas.ticket.registry.DefaultEncodedTicket;
import org.apereo.cas.util.serialization.AbstractJacksonBackedNonHjsonStringSerializer;

import org.springframework.context.ConfigurableApplicationContext;
import java.io.Serial;

/**
 * This is {@link EncodedTicketStringSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class EncodedTicketStringSerializer extends AbstractJacksonBackedNonHjsonStringSerializer<DefaultEncodedTicket> {
    @Serial
    private static final long serialVersionUID = 8959835299162115085L;

    public EncodedTicketStringSerializer(final ConfigurableApplicationContext applicationContext) {
        super(MINIMAL_PRETTY_PRINTER, applicationContext);
    }

    @Override
    public Class<DefaultEncodedTicket> getTypeToSerialize() {
        return DefaultEncodedTicket.class;
    }

}
