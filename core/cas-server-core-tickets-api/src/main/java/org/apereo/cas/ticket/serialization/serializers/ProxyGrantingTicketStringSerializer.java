package org.apereo.cas.ticket.serialization.serializers;

import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.util.serialization.BaseJacksonSerializer;

import org.springframework.context.ConfigurableApplicationContext;
import java.io.Serial;

/**
 * This is {@link ProxyGrantingTicketStringSerializer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class ProxyGrantingTicketStringSerializer extends BaseJacksonSerializer<ProxyGrantingTicketImpl> {
    @Serial
    private static final long serialVersionUID = 7089208351327601379L;

    public ProxyGrantingTicketStringSerializer(final ConfigurableApplicationContext applicationContext) {
        super(MINIMAL_PRETTY_PRINTER, applicationContext, ProxyGrantingTicketImpl.class);
    }
}
