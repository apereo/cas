package org.apereo.cas.ticket.serialization.serializers;

import module java.base;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.util.serialization.BaseJacksonSerializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link TransientSessionTicketStringSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class TransientSessionTicketStringSerializer extends BaseJacksonSerializer<TransientSessionTicketImpl> {
    @Serial
    private static final long serialVersionUID = 8959617299162115085L;

    public TransientSessionTicketStringSerializer(final ConfigurableApplicationContext applicationContext) {
        super(MINIMAL_PRETTY_PRINTER, applicationContext, TransientSessionTicketImpl.class);
    }
}
