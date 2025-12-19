package org.apereo.cas.ticket.serialization.serializers;

import module java.base;
import org.apereo.cas.ticket.ProxyTicketImpl;
import org.apereo.cas.util.serialization.BaseJacksonSerializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link ProxyTicketStringSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class ProxyTicketStringSerializer extends BaseJacksonSerializer<ProxyTicketImpl> {
    @Serial
    private static final long serialVersionUID = -6343596853082798477L;

    public ProxyTicketStringSerializer(final ConfigurableApplicationContext applicationContext) {
        super(MINIMAL_PRETTY_PRINTER, applicationContext, ProxyTicketImpl.class);
    }
}
